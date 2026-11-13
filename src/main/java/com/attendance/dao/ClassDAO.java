package com.attendance.dao;

import com.attendance.model.SchoolClass;
import com.attendance.util.MongoDBConnection;
import com.attendance.util.ValidationUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * MongoDB Data Access Object for managing academic classes and sections.
 */
public class ClassDAO {

    private MongoCollection<Document> collection() {
        return MongoDBConnection.getDatabase().getCollection("classes");
    }

    public String createClass(SchoolClass schoolClass) {
        ObjectId id = new ObjectId();
        Document doc = new Document("_id", id)
                .append("className", schoolClass.getClassName().trim())
                .append("section", schoolClass.getSection().trim().toUpperCase())
                .append("department", schoolClass.getDepartment().trim())
                .append("academicYear", schoolClass.getAcademicYear().trim())
                .append("createdAt", new Date());
        collection().insertOne(doc);
        schoolClass.setClassId(id.toHexString());
        return id.toHexString();
    }

    public List<SchoolClass> findAll() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> studentsCol = db.getCollection("students");

        List<SchoolClass> list = new ArrayList<>();
        for (Document doc : collection().find().sort(Sorts.ascending("className", "section"))) {
            SchoolClass sc = mapClass(doc);
            ObjectId oid = doc.getObjectId("_id");
            if (oid != null) {
                sc.setStudentCount((int) studentsCol.countDocuments(Filters.eq("classId", oid)));
            }
            list.add(sc);
        }
        return list;
    }

    public List<SchoolClass> findByTeacherId(String teacherId) {
        ObjectId teacherOid = ValidationUtil.parseObjectIdOrNull(teacherId);
        if (teacherOid == null) {
            return new ArrayList<>();
        }
        MongoDatabase db = MongoDBConnection.getDatabase();
        Set<ObjectId> classIds = new LinkedHashSet<>();

        Document teacherDoc = db.getCollection("teachers").find(Filters.eq("_id", teacherOid)).first();
        if (teacherDoc != null) {
            ObjectId assigned = teacherDoc.getObjectId("assignedClassId");
            if (assigned != null) {
                classIds.add(assigned);
            }
        }

        for (Document subDoc : db.getCollection("subjects").find(Filters.eq("teacherId", teacherOid))) {
            ObjectId cid = subDoc.getObjectId("classId");
            if (cid != null) {
                classIds.add(cid);
            }
        }

        if (classIds.isEmpty()) {
            return new ArrayList<>();
        }

        MongoCollection<Document> studentsCol = db.getCollection("students");
        List<SchoolClass> list = new ArrayList<>();
        for (Document doc : collection()
                .find(Filters.in("_id", classIds))
                .sort(Sorts.ascending("className", "section"))) {
            SchoolClass sc = mapClass(doc);
            ObjectId oid = doc.getObjectId("_id");
            sc.setStudentCount((int) studentsCol.countDocuments(Filters.eq("classId", oid)));
            list.add(sc);
        }
        return list;
    }

    public SchoolClass findById(String classId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(classId);
        if (oid == null) {
            return null;
        }
        Document doc = collection().find(Filters.eq("_id", oid)).first();
        return doc != null ? mapClass(doc) : null;
    }

    public boolean updateClass(SchoolClass schoolClass) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(schoolClass.getClassId());
        if (oid == null) {
            return false;
        }
        UpdateResult result = collection().updateOne(
                Filters.eq("_id", oid),
                Updates.combine(
                        Updates.set("className", schoolClass.getClassName().trim()),
                        Updates.set("section", schoolClass.getSection().trim().toUpperCase()),
                        Updates.set("department", schoolClass.getDepartment().trim()),
                        Updates.set("academicYear", schoolClass.getAcademicYear().trim())
                ));
        return result.getMatchedCount() > 0;
    }

    public boolean deleteClass(String classId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(classId);
        if (oid == null) {
            return false;
        }
        DeleteResult result = collection().deleteOne(Filters.eq("_id", oid));
        if (result.getDeletedCount() > 0) {
            MongoDatabase db = MongoDBConnection.getDatabase();
            db.getCollection("students").updateMany(Filters.eq("classId", oid), Updates.unset("classId"));
            db.getCollection("teachers").updateMany(Filters.eq("assignedClassId", oid), Updates.unset("assignedClassId"));
            db.getCollection("subjects").deleteMany(Filters.eq("classId", oid));
            return true;
        }
        return false;
    }

    public int countClasses() {
        return (int) collection().countDocuments();
    }

    private SchoolClass mapClass(Document doc) {
        SchoolClass c = new SchoolClass();
        ObjectId id = doc.getObjectId("_id");
        c.setClassId(id != null ? id.toHexString() : null);
        c.setClassName(doc.getString("className"));
        c.setSection(doc.getString("section"));
        c.setDepartment(doc.getString("department"));
        String year = doc.getString("academicYear");
        c.setAcademicYear(year != null ? year : "2026-2027");
        c.setCreatedAt(doc.getDate("createdAt"));
        return c;
    }
}
