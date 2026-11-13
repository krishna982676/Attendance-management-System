package com.attendance.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Manages the shared MongoClient singleton and database lifecycle for MongoDB.
 */
public final class MongoDBConnection {

    private static String connectionUri = "mongodb://localhost:27017";
    private static String databaseName = "attendance_management";
    private static volatile MongoClient mongoClient;
    private static volatile boolean configLoaded = false;

    private MongoDBConnection() {}

    private static synchronized void loadConfigIfNeeded() {
        if (configLoaded) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = MongoDBConnection.class.getClassLoader().getResourceAsStream("mongodb.properties")) {
            if (in != null) {
                props.load(in);
                connectionUri = props.getProperty("mongodb.uri", connectionUri).trim();
                databaseName = props.getProperty("mongodb.database", databaseName).trim();
            }
        } catch (IOException ignored) {
            // Fall back to default localhost:27017 settings
        }

        String envUri = System.getenv("MONGO_URI");
        String envDb = System.getenv("MONGO_DB");
        if (envUri != null && !envUri.isBlank()) {
            connectionUri = envUri.trim();
        }
        if (envDb != null && !envDb.isBlank()) {
            databaseName = envDb.trim();
        }
        configLoaded = true;
    }

    /**
     * Overrides the MongoDB URI and database name (useful for isolated tests) and resets the client.
     */
    public static synchronized void configure(String uri, String dbName) {
        close();
        connectionUri = uri;
        databaseName = dbName;
        configLoaded = true;
    }

    /**
     * Returns the shared MongoClient instance, creating it lazily if needed.
     */
    public static MongoClient getClient() {
        if (mongoClient == null) {
            synchronized (MongoDBConnection.class) {
                if (mongoClient == null) {
                    loadConfigIfNeeded();
                    MongoClientSettings settings = MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(connectionUri))
                            .applyToClusterSettings(builder ->
                                    builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                            .applyToSocketSettings(builder ->
                                    builder.connectTimeout(5, TimeUnit.SECONDS))
                            .build();
                    mongoClient = MongoClients.create(settings);
                }
            }
        }
        return mongoClient;
    }

    /**
     * Returns the configured MongoDatabase instance from the shared MongoClient.
     */
    public static MongoDatabase getDatabase() {
        loadConfigIfNeeded();
        return getClient().getDatabase(databaseName);
    }

    /**
     * Verifies live connectivity to MongoDB with a ping command.
     */
    public static boolean ping() {
        try {
            Document result = getDatabase().runCommand(new Document("ping", 1));
            return result.getDouble("ok") != null && result.getDouble("ok") == 1.0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Safely and idempotently ensures required indexes exist without dropping collections or documents.
     * Also migrates legacy attendance index (studentId + date) to (studentId + date + subjectId)
     * to support subject-wise attendance tracking.
     */
    public static void ensureIndexes() {
        MongoDatabase db = getDatabase();

        db.getCollection("users").createIndex(
                Indexes.ascending("username"),
                new IndexOptions().unique(true));

        db.getCollection("students").createIndex(
                Indexes.ascending("rollNumber"),
                new IndexOptions().unique(true));

        db.getCollection("teachers").createIndex(
                Indexes.ascending("employeeId"),
                new IndexOptions().unique(true));

        db.getCollection("subjects").createIndex(
                Indexes.ascending("subjectCode"),
                new IndexOptions().unique(true));

        MongoCollection<Document> attendanceCol = db.getCollection("attendance");
        boolean hasLegacyIndex = false;
        for (Document idx : attendanceCol.listIndexes()) {
            String name = idx.getString("name");
            if ("studentId_1_date_1".equals(name)) {
                hasLegacyIndex = true;
                break;
            }
        }
        if (hasLegacyIndex) {
            attendanceCol.dropIndex("studentId_1_date_1");
        }

        attendanceCol.createIndex(
                Indexes.compoundIndex(
                        Indexes.ascending("studentId"),
                        Indexes.ascending("date"),
                        Indexes.ascending("subjectId")),
                new IndexOptions().unique(true));

        attendanceCol.createIndex(
                Indexes.compoundIndex(Indexes.ascending("classId"), Indexes.ascending("date")));
        attendanceCol.createIndex(Indexes.ascending("date"));
    }

    /**
     * Closes the shared MongoClient cleanly when the web application shuts down.
     */
    public static synchronized void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } finally {
                mongoClient = null;
            }
        }
    }
}

