package com.attendance.listener;

import com.attendance.dao.AttendanceDAO;
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
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes MongoDB indexes and default starter accounts on application startup,
 * and cleanly closes the shared MongoClient on shutdown.
 */
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppLifecycleListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            MongoDBConnection.ensureIndexes();
            seedInitialDataIfEmpty();
            LOGGER.info("MongoDB connection initialized and indexes verified.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize MongoDB during startup: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoDBConnection.close();
        LOGGER.info("MongoDB connection closed on application shutdown.");
    }

    /**
     * Seeds initial Admin, Teacher, Class, Subject, and Student records ONLY if the users
     * collection is empty. Never drops collections or modifies existing documents (such as TEST001).
     */
    public static void seedInitialDataIfEmpty() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        if (db.getCollection("users").countDocuments() > 0) {
            return;
        }

        UserDAO userDAO = new UserDAO();
        ClassDAO classDAO = new ClassDAO();
        TeacherDAO teacherDAO = new TeacherDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        StudentDAO studentDAO = new StudentDAO();
        AttendanceDAO attendanceDAO = new AttendanceDAO();

        // 1. Admin User
        User admin = new User();
        admin.setUsername("admin");
        admin.setRole("ADMIN");
        admin.setFullName("System Administrator");
        admin.setEmail("admin@college.edu");
        admin.setActive(true);
        userDAO.createUser(admin, "admin123");

        // 2. Classes
        SchoolClass cseA = new SchoolClass("B.Tech CSE Sem 5", "A", "Computer Science & Engineering", "2026-2027");
        String cseAId = classDAO.createClass(cseA);

        SchoolClass cseB = new SchoolClass("B.Tech CSE Sem 5", "B", "Computer Science & Engineering", "2026-2027");
        String cseBId = classDAO.createClass(cseB);

        // 3. Teachers + Login Accounts
        Teacher t1 = new Teacher();
        t1.setEmployeeId("EMP-CSE-101");
        t1.setName("Prof. Aarav Sharma");
        t1.setEmail("aarav.sharma@college.edu");
        t1.setPhone("9876501001");
        t1.setDepartment("Computer Science & Engineering");
        t1.setAssignedClassId(cseAId);
        String t1Id = teacherDAO.createTeacher(t1, "teacher1", "teacher123");

        Teacher t2 = new Teacher();
        t2.setEmployeeId("EMP-CSE-102");
        t2.setName("Prof. Priya Verma");
        t2.setEmail("priya.verma@college.edu");
        t2.setPhone("9876501002");
        t2.setDepartment("Computer Science & Engineering");
        t2.setAssignedClassId(cseBId);
        String t2Id = teacherDAO.createTeacher(t2, "teacher2", "teacher123");

        // 4. Subjects
        Subject sub1 = new Subject();
        sub1.setSubjectCode("CS501");
        sub1.setSubjectName("Database Management Systems");
        sub1.setClassId(cseAId);
        sub1.setTeacherId(t1Id);
        String sub1Id = subjectDAO.createSubject(sub1);

        Subject sub2 = new Subject();
        sub2.setSubjectCode("CS502");
        sub2.setSubjectName("Java Web Technologies");
        sub2.setClassId(cseAId);
        sub2.setTeacherId(t1Id);
        subjectDAO.createSubject(sub2);

        Subject sub3 = new Subject();
        sub3.setSubjectCode("CS503");
        sub3.setSubjectName("Operating Systems");
        sub3.setClassId(cseBId);
        sub3.setTeacherId(t2Id);
        subjectDAO.createSubject(sub3);

        // 5. Students + Portal Accounts (preserving any existing TEST001 document)
        String s1Id = createSeedStudentIfMissing(studentDAO, "Rohan Gupta", "CS501", "rohan.gupta@college.edu",
                "9811100001", cseAId, "Computer Science & Engineering", "student1", "student123");
        String s2Id = createSeedStudentIfMissing(studentDAO, "Ananya Singh", "CS502", "ananya.singh@college.edu",
                "9811100002", cseAId, "Computer Science & Engineering", "student2", "student123");
        String s3Id = createSeedStudentIfMissing(studentDAO, "Vikram Patel", "CS503", "vikram.patel@college.edu",
                "9811100003", cseAId, "Computer Science & Engineering", "student3", "student123");
        createSeedStudentIfMissing(studentDAO, "Sneha Nair", "CS504", "sneha.nair@college.edu",
                "9811100004", cseBId, "Computer Science & Engineering", "student4", "student123");

        // 6. Seed recent attendance entries for CSE-A so dashboards and <75% reports show live DB stats
        Teacher teacherObj = teacherDAO.findById(t1Id);
        String markerUserId = teacherObj != null ? teacherObj.getUserId() : null;
        if (s1Id != null && s2Id != null && s3Id != null && markerUserId != null) {
            LocalDate today = LocalDate.now();
            for (int d = 3; d >= 0; d--) {
                String dateStr = today.minusDays(d).toString();
                Map<String, String> statuses = new LinkedHashMap<>();
                statuses.put(s1Id, "Present");
                statuses.put(s2Id, d == 2 ? "Absent" : "Present");
                statuses.put(s3Id, d == 0 ? "Present" : "Absent");
                try {
                    attendanceDAO.saveClassAttendance(cseAId, sub1Id, dateStr, markerUserId, statuses, Map.of(), false);
                } catch (Exception ignored) {
                    // Skip if already present
                }
            }
        }
    }

    private static String createSeedStudentIfMissing(StudentDAO studentDAO,
                                                     String name,
                                                     String rollNumber,
                                                     String email,
                                                     String phone,
                                                     String classId,
                                                     String department,
                                                     String username,
                                                     String password) {
        if (studentDAO.rollNumberExists(rollNumber, null)) {
            return null;
        }
        Student s = new Student();
        s.setName(name);
        s.setRollNumber(rollNumber);
        s.setEmail(email);
        s.setPhone(phone);
        s.setClassId(classId);
        s.setDepartment(department);
        return studentDAO.createStudent(s, username, password);
    }
}

