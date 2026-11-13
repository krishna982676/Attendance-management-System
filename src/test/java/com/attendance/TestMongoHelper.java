package com.attendance;

import com.attendance.dao.ClassDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.SchoolClass;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.model.Teacher;
import com.attendance.model.User;
import com.attendance.util.MongoDBConnection;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/**
 * Helper to prepare and reset an isolated test database in MongoDB for tests.
 */
public final class TestMongoHelper {

    public static final String TEST_DB = "attendance_management_test";
    private static volatile Boolean mongoAvailable = null;

    private TestMongoHelper() {}

    public static synchronized boolean isMongoAvailable() {
        if (mongoAvailable != null) {
            return mongoAvailable;
        }
        try {
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                    .applyToClusterSettings(b -> b.serverSelectionTimeout(500, TimeUnit.MILLISECONDS))
                    .applyToSocketSettings(b -> b.connectTimeout(500, TimeUnit.MILLISECONDS))
                    .build();
            try (MongoClient testClient = MongoClients.create(settings)) {
                Document ping = testClient.getDatabase("admin").runCommand(new Document("ping", 1));
                mongoAvailable = (ping.getDouble("ok") != null && ping.getDouble("ok") == 1.0);
            }
        } catch (Exception e) {
            mongoAvailable = false;
        }
        return mongoAvailable;
    }

    public static void resetDatabase() {
        MongoDBConnection.configure("mongodb://localhost:27017", TEST_DB);
        MongoDatabase db = MongoDBConnection.getDatabase();
        db.drop();
        MongoDBConnection.ensureIndexes();

        UserDAO userDAO = new UserDAO();
        ClassDAO classDAO = new ClassDAO();
        TeacherDAO teacherDAO = new TeacherDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        StudentDAO studentDAO = new StudentDAO();

        User admin = new User();
        admin.setUsername("admin");
        admin.setRole("ADMIN");
        admin.setFullName("System Admin");
        admin.setEmail("admin@college.edu");
        admin.setActive(true);
        userDAO.createUser(admin, "admin123");

        SchoolClass c1 = new SchoolClass("B.Tech CSE Sem 5", "A", "Computer Science", "2026-2027");
        String c1Id = classDAO.createClass(c1);

        SchoolClass c2 = new SchoolClass("B.Tech CSE Sem 5", "B", "Computer Science", "2026-2027");
        classDAO.createClass(c2);

        Teacher t1 = new Teacher();
        t1.setEmployeeId("EMP-101");
        t1.setName("Prof. Aarav Sharma");
        t1.setEmail("aarav@college.edu");
        t1.setPhone("9876501001");
        t1.setDepartment("Computer Science");
        t1.setAssignedClassId(c1Id);
        String t1Id = teacherDAO.createTeacher(t1, "teacher1", "teacher123");

        Subject s1 = new Subject();
        s1.setSubjectCode("CS501");
        s1.setSubjectName("Database Management Systems");
        s1.setClassId(c1Id);
        s1.setTeacherId(t1Id);
        subjectDAO.createSubject(s1);

        Student stu1 = new Student();
        stu1.setName("Rohan Gupta");
        stu1.setRollNumber("CS501");
        stu1.setEmail("rohan@college.edu");
        stu1.setPhone("9811100001");
        stu1.setClassId(c1Id);
        stu1.setDepartment("Computer Science");
        studentDAO.createStudent(stu1, "student1", "student123");

        Student stu2 = new Student();
        stu2.setName("Vikram Patel");
        stu2.setRollNumber("CS502");
        stu2.setEmail("vikram@college.edu");
        stu2.setPhone("9811100002");
        stu2.setClassId(c1Id);
        stu2.setDepartment("Computer Science");
        studentDAO.createStudent(stu2, null, null);
    }
}
