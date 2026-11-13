package com.attendance.dao;

import com.attendance.TestMongoHelper;
import com.attendance.model.SchoolClass;
import com.attendance.model.Student;
import com.attendance.model.Teacher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentTeacherClassDAOTest {

    private StudentDAO studentDAO;
    private TeacherDAO teacherDAO;
    private ClassDAO classDAO;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(TestMongoHelper.isMongoAvailable(), "MongoDB not available or socket restricted in sandbox");
        TestMongoHelper.resetDatabase();
        studentDAO = new StudentDAO();
        teacherDAO = new TeacherDAO();
        classDAO = new ClassDAO();
    }

    @Test
    void testAddUpdateSearchAndDeleteStudent() {
        List<SchoolClass> classes = classDAO.findAll();
        assertFalse(classes.isEmpty());
        String classId = classes.get(0).getClassId();

        Student s = new Student();
        s.setName("Ananya Singh");
        s.setRollNumber("CS509");
        s.setEmail("ananya@college.edu");
        s.setPhone("9811100009");
        s.setClassId(classId);
        s.setDepartment("Computer Science");

        String newId = studentDAO.createStudent(s, "ananya509", "pass1234");
        assertNotNull(newId);
        assertTrue(studentDAO.countStudents() >= 3);

        // Search by name or roll number
        List<Student> foundByRoll = studentDAO.findAll("CS509", null);
        assertEquals(1, foundByRoll.size());
        assertEquals("Ananya Singh", foundByRoll.get(0).getName());

        // Update student
        Student toUpdate = foundByRoll.get(0);
        toUpdate.setName("Ananya Sharma");
        assertTrue(studentDAO.updateStudent(toUpdate));

        Student updated = studentDAO.findById(newId);
        assertEquals("Ananya Sharma", updated.getName());

        // Delete student
        assertTrue(studentDAO.deleteStudent(newId));
        assertNull(studentDAO.findById(newId));
    }

    @Test
    void testTeacherCreationAndClassLookup() {
        List<SchoolClass> classes = classDAO.findAll();
        String classId = classes.get(classes.size() - 1).getClassId();

        Teacher t = new Teacher();
        t.setEmployeeId("EMP-202");
        t.setName("Prof. Priya Verma");
        t.setEmail("priya@college.edu");
        t.setPhone("9876501002");
        t.setDepartment("Computer Science");
        t.setAssignedClassId(classId);

        String tid = teacherDAO.createTeacher(t, "teacher2", "teacher123");
        assertNotNull(tid);
        assertTrue(teacherDAO.countTeachers() >= 2);
        assertEquals(1, classDAO.findByTeacherId(tid).size());
    }
}
