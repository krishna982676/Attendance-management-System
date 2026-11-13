package com.attendance.dao;

import com.attendance.model.Subject;
import com.attendance.util.MongoDBConnection;
import com.attendance.util.ValidationUtil;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB Data Access Object for managing subjects assigned to classes and teachers.
 */
public class SubjectDAO {

    private MongoCollection<Document> collection() {
        return MongoDBConnection.getDatabase().getCollection("subjects");
    }

    public String createSubject(Subject subject) {
        ObjectId id = new ObjectId();
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(subject.getClassId());
        ObjectId teacherOid = ValidationUtil.parseObjectIdOrNull(subject.getTeacherId());

        Document doc = new Document("_id", id)
                .append("subjectCode", subject.getSubjectCode().trim().toUpperCase())
                .append("subjectName", subject.getSubjectName().trim())
                .append("classId", classOid)
                .append("teacherId", teacherOid)
                .append("createdAt", new Date());
        try {
            collection().insertOne(doc);
            subject.setSubjectId(id.toHexString());
            return id.toHexString();
        } catch (MongoWriteException e) {
            throw new IllegalStateException("Subject code already exists: " + subject.getSubjectCode(), e);
        }
    }

    public List<Subject> findAll() {
        return querySubjects(new Document());
    }

    public Subject findById(String subjectId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(subjectId);
        if (oid == null) {
            return null;
        }
        List<Subject> results = querySubjects(Filters.eq("_id", oid));
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Subject> findByClassId(String classId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(classId);
        if (oid == null) {
            return new ArrayList<>();
        }
        return querySubjects(Filters.eq("classId", oid));
    }

    public List<Subject> findByTeacherId(String teacherId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(teacherId);
        if (oid == null) {
            return new ArrayList<>();
        }
        return querySubjects(Filters.eq("teacherId", oid));
    }

    public boolean updateSubject(Subject subject) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(subject.getSubjectId());
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(subject.getClassId());
        if (oid == null || classOid == null) {
            return false;
        }
        ObjectId teacherOid = ValidationUtil.parseObjectIdOrNull(subject.getTeacherId());
        Bson teacherUpdate = teacherOid != null
                ? Updates.set("teacherId", teacherOid)
                : Updates.unset("teacherId");

        UpdateResult result = collection().updateOne(
                Filters.eq("_id", oid),
                Updates.combine(
                        Updates.set("subjectCode", subject.getSubjectCode().trim().toUpperCase()),
                        Updates.set("subjectName", subject.getSubjectName().trim()),
                        Updates.set("classId", classOid),
                        teacherUpdate
                ));
        return result.getMatchedCount() > 0;
    }

    public boolean deleteSubject(String subjectId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(subjectId);
        if (oid == null) {
            return false;
        }
        DeleteResult result = collection().deleteOne(Filters.eq("_id", oid));
        return result.getDeletedCount() > 0;
    }

    private List<Subject> querySubjects(Bson filter) {
        MongoDatabase db = MongoDBConnection.getDatabase();
        Map<ObjectId, String> classNames = loadClassNames(db);
        Map<ObjectId, String> teacherNames = loadTeacherNames(db);

        List<Subject> list = new ArrayList<>();
        for (Document doc : collection().find(filter).sort(Sorts.ascending("subjectCode"))) {
            Subject s = new Subject();
            ObjectId id = doc.getObjectId("_id");
            ObjectId cid = doc.getObjectId("classId");
            ObjectId tid = doc.getObjectId("teacherId");

            s.setSubjectId(id != null ? id.toHexString() : null);
            s.setSubjectCode(doc.getString("subjectCode"));
            s.setSubjectName(doc.getString("subjectName"));
            s.setClassId(cid != null ? cid.toHexString() : null);
            s.setTeacherId(tid != null ? tid.toHexString() : null);
            s.setClassDisplayName(cid != null ? classNames.getOrDefault(cid, "Unknown Class") : "Unassigned");
            s.setTeacherName(tid != null ? teacherNames.get(tid) : null);
            s.setCreatedAt(doc.getDate("createdAt"));
            list.add(s);
        }
        return list;
    }

    private Map<ObjectId, String> loadClassNames(MongoDatabase db) {
        Map<ObjectId, String> map = new HashMap<>();
        for (Document doc : db.getCollection("classes").find()) {
            ObjectId id = doc.getObjectId("_id");
            if (id != null) {
                map.put(id, doc.getString("className") + " - " + doc.getString("section"));
            }
        }
        return map;
    }

    private Map<ObjectId, String> loadTeacherNames(MongoDatabase db) {
        Map<ObjectId, String> map = new HashMap<>();
        for (Document doc : db.getCollection("teachers").find()) {
            ObjectId id = doc.getObjectId("_id");
            if (id != null) {
                map.put(id, doc.getString("name"));
            }
        }
        return map;
    }
}
