package com.attendance.dao;

import com.attendance.model.Student;
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
 * MongoDB Data Access Object for student CRUD, search, and class assignment.
 */
public class StudentDAO {

    private MongoCollection<Document> collection() {
        return MongoDBConnection.getDatabase().getCollection("students");
    }

    public List<Student> findAll(String search, String classId) {
        List<Bson> filters = new ArrayList<>();

        if (!ValidationUtil.isBlank(search)) {
            String escaped = Pattern.quote(search.trim());
            Pattern regex = Pattern.compile(escaped, Pattern.CASE_INSENSITIVE);
            filters.add(Filters.or(
                    Filters.regex("name", regex),
                    Filters.regex("rollNumber", regex),
                    Filters.regex("department", regex)
            ));
        }

        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        if (classOid != null) {
            filters.add(Filters.eq("classId", classOid));
        }

        Bson combined = filters.isEmpty() ? new Document() : Filters.and(filters);
        return queryStudents(combined);
    }

    public List<Student> findByClassId(String classId) {
        if (!ValidationUtil.isValidObjectId(classId)) {
            return new ArrayList<>();
        }
        return findAll(null, classId);
    }

    public Student findById(String studentId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(studentId);
        if (oid == null) {
            return null;
        }
        List<Student> list = queryStudents(Filters.eq("_id", oid));
        return list.isEmpty() ? null : list.get(0);
    }

    public Student findByUserId(String userId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(userId);
        if (oid == null) {
            return null;
        }
        List<Student> list = queryStudents(Filters.eq("userId", oid));
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean rollNumberExists(String rollNumber, String excludeStudentId) {
        if (ValidationUtil.isBlank(rollNumber)) {
            return false;
        }
        Pattern exactCaseInsensitive = Pattern.compile("^" + Pattern.quote(rollNumber.trim()) + "$", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("rollNumber", exactCaseInsensitive);
        ObjectId excludeOid = ValidationUtil.parseObjectIdOrNull(excludeStudentId);
        if (excludeOid != null) {
            filter = Filters.and(filter, Filters.ne("_id", excludeOid));
        }
        return collection().countDocuments(filter) > 0;
    }

    public boolean emailExists(String email, String excludeStudentId) {
        if (ValidationUtil.isBlank(email)) {
            return false;
        }
        Pattern exactCaseInsensitive = Pattern.compile("^" + Pattern.quote(email.trim()) + "$", Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.regex("email", exactCaseInsensitive);
        ObjectId excludeOid = ValidationUtil.parseObjectIdOrNull(excludeStudentId);
        if (excludeOid != null) {
            filter = Filters.and(filter, Filters.ne("_id", excludeOid));
        }
        return collection().countDocuments(filter) > 0;
    }

    /**
     * Creates a student document and optionally creates a linked STUDENT login user account.
     */
    public String createStudent(Student student, String loginUsername, String loginPassword) {
        String createdUserId = null;
        UserDAO userDAO = new UserDAO();
        if (!ValidationUtil.isBlank(loginUsername) && !ValidationUtil.isBlank(loginPassword)) {
            User user = new User();
            user.setUsername(loginUsername.trim());
            user.setRole("STUDENT");
            user.setFullName(student.getName().trim());
            user.setEmail(student.getEmail().trim().toLowerCase());
            user.setActive(true);
            createdUserId = userDAO.createUser(user, loginPassword);
        } else if (ValidationUtil.isValidObjectId(student.getUserId())) {
            createdUserId = student.getUserId();
        }

        ObjectId id = new ObjectId();
        ObjectId userOid = ValidationUtil.parseObjectIdOrNull(createdUserId);
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(student.getClassId());

        Document doc = new Document("_id", id)
                .append("name", student.getName().trim())
                .append("rollNumber", student.getRollNumber().trim().toUpperCase())
                .append("email", student.getEmail().trim().toLowerCase())
                .append("phone", ValidationUtil.trim(student.getPhone()))
                .append("department", ValidationUtil.trim(student.getDepartment()))
                .append("createdAt", new Date());
        if (userOid != null) {
            doc.append("userId", userOid);
        }
        if (classOid != null) {
            doc.append("classId", classOid);
        }

        try {
            collection().insertOne(doc);
            student.setStudentId(id.toHexString());
            student.setUserId(createdUserId);
            return id.toHexString();
        } catch (MongoWriteException e) {
            if (createdUserId != null && !ValidationUtil.isBlank(loginUsername)) {
                userDAO.deleteUser(createdUserId);
            }
            throw new IllegalStateException("Duplicate roll number or student record: " + student.getRollNumber(), e);
        }
    }

    public boolean updateStudent(Student student) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(student.getStudentId());
        if (oid == null) {
            return false;
        }
        Document existing = collection().find(Filters.eq("_id", oid)).first();
        if (existing == null) {
            return false;
        }

        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(student.getClassId());
        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("name", student.getName().trim()));
        updates.add(Updates.set("rollNumber", student.getRollNumber().trim().toUpperCase()));
        updates.add(Updates.set("email", student.getEmail().trim().toLowerCase()));
        updates.add(Updates.set("phone", ValidationUtil.trim(student.getPhone())));
        updates.add(Updates.set("department", ValidationUtil.trim(student.getDepartment())));
        updates.add(classOid != null ? Updates.set("classId", classOid) : Updates.unset("classId"));

        UpdateResult result = collection().updateOne(Filters.eq("_id", oid), Updates.combine(updates));

        ObjectId userOid = existing.getObjectId("userId");
        if (userOid != null) {
            MongoDBConnection.getDatabase().getCollection("users").updateOne(
                    Filters.eq("_id", userOid),
                    Updates.combine(
                            Updates.set("fullName", student.getName().trim()),
                            Updates.set("email", student.getEmail().trim().toLowerCase())
                    ));
        }
        return result.getMatchedCount() > 0;
    }

    public boolean deleteStudent(String studentId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(studentId);
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
            db.getCollection("attendance").deleteMany(Filters.eq("studentId", oid));
            ObjectId userOid = existing.getObjectId("userId");
            if (userOid != null) {
                db.getCollection("users").deleteOne(Filters.eq("_id", userOid));
            }
            return true;
        }
        return false;
    }

    public int countStudents() {
        return (int) collection().countDocuments();
    }

    private List<Student> queryStudents(Bson filter) {
        MongoDatabase db = MongoDBConnection.getDatabase();
        Map<ObjectId, String> classNames = loadClassNames(db);
        Map<ObjectId, String> usernames = loadUsernames(db);

        List<Student> students = new ArrayList<>();
        for (Document doc : collection().find(filter).sort(Sorts.ascending("rollNumber"))) {
            students.add(mapStudent(doc, classNames, usernames));
        }
        return students;
    }

    private Student mapStudent(Document doc, Map<ObjectId, String> classNames, Map<ObjectId, String> usernames) {
        Student s = new Student();
        ObjectId id = doc.getObjectId("_id");
        ObjectId uid = doc.getObjectId("userId");
        ObjectId cid = doc.getObjectId("classId");

        s.setStudentId(id != null ? id.toHexString() : null);
        s.setUserId(uid != null ? uid.toHexString() : null);
        s.setName(doc.getString("name"));
        s.setRollNumber(doc.getString("rollNumber"));
        s.setEmail(doc.getString("email"));
        String phone = doc.getString("phone");
        s.setPhone(phone != null ? phone : "");
        s.setClassId(cid != null ? cid.toHexString() : null);
        s.setClassName(cid != null ? classNames.get(cid) : null);
        String dept = doc.getString("department");
        s.setDepartment(dept != null ? dept : "");
        s.setUsername(uid != null ? usernames.get(uid) : null);
        s.setCreatedAt(doc.getDate("createdAt"));
        return s;
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
