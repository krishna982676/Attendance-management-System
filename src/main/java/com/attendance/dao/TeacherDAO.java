package com.attendance.dao;

import com.attendance.model.Teacher;
import com.attendance.model.User;
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
import java.util.regex.Pattern;

/**
 * MongoDB Data Access Object for teacher CRUD and class assignment.
 */
public class TeacherDAO {

    private MongoCollection<Document> collection() {
        return MongoDBConnection.getDatabase().getCollection("teachers");
    }

    public List<Teacher> findAll(String search) {
        Bson filter = new Document();
        if (!ValidationUtil.isBlank(search)) {
            Pattern regex = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            filter = Filters.or(
                    Filters.regex("name", regex),
                    Filters.regex("employeeId", regex),
                    Filters.regex("department", regex)
            );
        }
        return queryTeachers(filter);
    }

    public Teacher findById(String teacherId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(teacherId);
        if (oid == null) {
            return null;
        }
        List<Teacher> list = queryTeachers(Filters.eq("_id", oid));
        return list.isEmpty() ? null : list.get(0);
    }

    public Teacher findByUserId(String userId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(userId);
        if (oid == null) {
            return null;
        }
        List<Teacher> list = queryTeachers(Filters.eq("userId", oid));
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean employeeIdExists(String employeeId, String excludeTeacherId) {
        if (ValidationUtil.isBlank(employeeId)) {
            return false;
        }
        Pattern exact = Pattern.compile("^" + Pattern.quote(employeeId.trim()) + "$", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("employeeId", exact);
        ObjectId excludeOid = ValidationUtil.parseObjectIdOrNull(excludeTeacherId);
        if (excludeOid != null) {
            filter = Filters.and(filter, Filters.ne("_id", excludeOid));
        }
        return collection().countDocuments(filter) > 0;
    }

    public String createTeacher(Teacher teacher, String loginUsername, String loginPassword) {
        String createdUserId = null;
        UserDAO userDAO = new UserDAO();
        if (!ValidationUtil.isBlank(loginUsername) && !ValidationUtil.isBlank(loginPassword)) {
            User user = new User();
            user.setUsername(loginUsername.trim());
            user.setRole("TEACHER");
            user.setFullName(teacher.getName().trim());
            user.setEmail(teacher.getEmail().trim().toLowerCase());
            user.setActive(true);
            createdUserId = userDAO.createUser(user, loginPassword);
        } else if (ValidationUtil.isValidObjectId(teacher.getUserId())) {
            createdUserId = teacher.getUserId();
        }

        ObjectId id = new ObjectId();
        ObjectId userOid = ValidationUtil.parseObjectIdOrNull(createdUserId);
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(teacher.getAssignedClassId());

        Document doc = new Document("_id", id)
                .append("employeeId", teacher.getEmployeeId().trim().toUpperCase())
                .append("name", teacher.getName().trim())
                .append("email", teacher.getEmail().trim().toLowerCase())
                .append("phone", ValidationUtil.trim(teacher.getPhone()))
                .append("department", ValidationUtil.trim(teacher.getDepartment()))
                .append("createdAt", new Date());
        if (userOid != null) {
            doc.append("userId", userOid);
        }
        if (classOid != null) {
            doc.append("assignedClassId", classOid);
        }

        try {
            collection().insertOne(doc);
            teacher.setTeacherId(id.toHexString());
            teacher.setUserId(createdUserId);
            return id.toHexString();
        } catch (MongoWriteException e) {
            if (createdUserId != null && !ValidationUtil.isBlank(loginUsername)) {
                userDAO.deleteUser(createdUserId);
            }
            throw new IllegalStateException("Duplicate employee ID: " + teacher.getEmployeeId(), e);
        }
    }

    public boolean updateTeacher(Teacher teacher) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(teacher.getTeacherId());
        if (oid == null) {
            return false;
        }
        Document existing = collection().find(Filters.eq("_id", oid)).first();
        if (existing == null) {
            return false;
        }

        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(teacher.getAssignedClassId());
        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("employeeId", teacher.getEmployeeId().trim().toUpperCase()));
        updates.add(Updates.set("name", teacher.getName().trim()));
        updates.add(Updates.set("email", teacher.getEmail().trim().toLowerCase()));
        updates.add(Updates.set("phone", ValidationUtil.trim(teacher.getPhone())));
        updates.add(Updates.set("department", ValidationUtil.trim(teacher.getDepartment())));
        updates.add(classOid != null ? Updates.set("assignedClassId", classOid) : Updates.unset("assignedClassId"));

        UpdateResult result = collection().updateOne(Filters.eq("_id", oid), Updates.combine(updates));

        ObjectId userOid = existing.getObjectId("userId");
        if (userOid != null) {
            MongoDBConnection.getDatabase().getCollection("users").updateOne(
                    Filters.eq("_id", userOid),
                    Updates.combine(
                            Updates.set("fullName", teacher.getName().trim()),
                            Updates.set("email", teacher.getEmail().trim().toLowerCase())
                    ));
        }
        return result.getMatchedCount() > 0;
    }

    public boolean deleteTeacher(String teacherId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(teacherId);
        if (oid == null) {
            return false;
        }
        Document existing = collection().find(Filters.eq("_id", oid)).first();
        if (existing == null) {
            return false;
        }

        DeleteResult result = collection().deleteOne(Filters.eq("_id", oid));
        if (result.getDeletedCount() > 0) {
            MongoDatabase db = MongoDBConnection.getDatabase();
            db.getCollection("subjects").updateMany(Filters.eq("teacherId", oid), Updates.unset("teacherId"));
            ObjectId userOid = existing.getObjectId("userId");
            if (userOid != null) {
                db.getCollection("users").deleteOne(Filters.eq("_id", userOid));
            }
            return true;
        }
        return false;
    }

    public int countTeachers() {
        return (int) collection().countDocuments();
    }

    private List<Teacher> queryTeachers(Bson filter) {
        MongoDatabase db = MongoDBConnection.getDatabase();
        Map<ObjectId, String> classNames = loadClassNames(db);
        Map<ObjectId, String> usernames = loadUsernames(db);

        List<Teacher> teachers = new ArrayList<>();
        for (Document doc : collection().find(filter).sort(Sorts.ascending("name"))) {
            Teacher t = new Teacher();
            ObjectId id = doc.getObjectId("_id");
            ObjectId uid = doc.getObjectId("userId");
            ObjectId cid = doc.getObjectId("assignedClassId");

            t.setTeacherId(id != null ? id.toHexString() : null);
            t.setUserId(uid != null ? uid.toHexString() : null);
            t.setEmployeeId(doc.getString("employeeId"));
            t.setName(doc.getString("name"));
            t.setEmail(doc.getString("email"));
            String phone = doc.getString("phone");
            t.setPhone(phone != null ? phone : "");
            String dept = doc.getString("department");
            t.setDepartment(dept != null ? dept : "");
            t.setAssignedClassId(cid != null ? cid.toHexString() : null);
            t.setAssignedClassName(cid != null ? classNames.get(cid) : null);
            t.setUsername(uid != null ? usernames.get(uid) : null);
            t.setCreatedAt(doc.getDate("createdAt"));
            teachers.add(t);
        }
        return teachers;
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

    private Map<ObjectId, String> loadUsernames(MongoDatabase db) {
        Map<ObjectId, String> map = new HashMap<>();
        for (Document doc : db.getCollection("users").find()) {
            ObjectId id = doc.getObjectId("_id");
            if (id != null) {
                map.put(id, doc.getString("username"));
            }
        }
        return map;
    }
}
