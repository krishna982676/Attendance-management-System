package com.attendance.dao;

import com.attendance.model.User;
import com.attendance.util.MongoDBConnection;
import com.attendance.util.PasswordUtil;
import com.attendance.util.ValidationUtil;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MongoDB Data Access Object for user authentication and account management.
 */
public class UserDAO {

    private MongoCollection<Document> collection() {
        return MongoDBConnection.getDatabase().getCollection("users");
    }

    public String createUser(User user, String plainPassword) {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("username", user.getUsername().trim())
                .append("password", PasswordUtil.hashPassword(plainPassword))
                .append("role", user.getRole().trim().toUpperCase())
                .append("fullName", user.getFullName().trim())
                .append("email", user.getEmail().trim().toLowerCase())
                .append("active", user.isActive())
                .append("createdAt", new Date());
        try {
            collection().insertOne(doc);
            user.setUserId(id.toHexString());
            return id.toHexString();
        } catch (MongoWriteException e) {
            throw new IllegalStateException("Username already exists: " + user.getUsername(), e);
        }
    }

    public User authenticate(String username, String plainPassword) {
        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(plainPassword)) {
            return null;
        }
        Document doc = collection().find(Filters.and(
                Filters.eq("username", username.trim()),
                Filters.eq("active", true)
        )).first();

        if (doc == null) {
            return null;
        }
        String storedHash = doc.getString("password");
        if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
            return mapUser(doc);
        }
        return null;
    }

    public User findByUsername(String username) {
        if (ValidationUtil.isBlank(username)) {
            return null;
        }
        Document doc = collection().find(Filters.eq("username", username.trim())).first();
        return doc != null ? mapUser(doc) : null;
    }

    public User findById(String userId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(userId);
        if (oid == null) {
            return null;
        }
        Document doc = collection().find(Filters.eq("_id", oid)).first();
        return doc != null ? mapUser(doc) : null;
    }

    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        if (ValidationUtil.isBlank(role)) {
            return users;
        }
        for (Document doc : collection()
                .find(Filters.eq("role", role.trim().toUpperCase()))
                .sort(Sorts.ascending("fullName"))) {
            users.add(mapUser(doc));
        }
        return users;
    }

    public boolean updateUser(User user) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(user.getUserId());
        if (oid == null) {
            return false;
        }
        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("fullName", user.getFullName().trim()));
        updates.add(Updates.set("email", user.getEmail().trim().toLowerCase()));
        updates.add(Updates.set("role", user.getRole().trim().toUpperCase()));
        updates.add(Updates.set("active", user.isActive()));
        if (!ValidationUtil.isBlank(user.getPassword())) {
            updates.add(Updates.set("password", PasswordUtil.hashPassword(user.getPassword())));
        }
        UpdateResult result = collection().updateOne(Filters.eq("_id", oid), Updates.combine(updates));
        return result.getMatchedCount() > 0;
    }

    public boolean deleteUser(String userId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(userId);
        if (oid == null) {
            return false;
        }
        DeleteResult result = collection().deleteOne(Filters.eq("_id", oid));
        return result.getDeletedCount() > 0;
    }

    public boolean usernameExists(String username, String excludeUserId) {
        if (ValidationUtil.isBlank(username)) {
            return false;
        }
        Bson filter = Filters.eq("username", username.trim());
        ObjectId excludeOid = ValidationUtil.parseObjectIdOrNull(excludeUserId);
        if (excludeOid != null) {
            filter = Filters.and(filter, Filters.ne("_id", excludeOid));
        }
        return collection().countDocuments(filter) > 0;
    }

    private User mapUser(Document doc) {
        User u = new User();
        ObjectId id = doc.getObjectId("_id");
        u.setUserId(id != null ? id.toHexString() : null);
        u.setUsername(doc.getString("username"));
        u.setRole(doc.getString("role"));
        u.setFullName(doc.getString("fullName"));
        u.setEmail(doc.getString("email"));
        Boolean active = doc.getBoolean("active");
        u.setActive(active == null || active);
        u.setCreatedAt(doc.getDate("createdAt"));
        return u;
    }
}
