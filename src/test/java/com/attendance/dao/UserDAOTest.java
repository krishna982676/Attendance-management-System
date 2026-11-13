package com.attendance.dao;

import com.attendance.TestMongoHelper;
import com.attendance.model.User;
import com.attendance.util.PasswordUtil;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(TestMongoHelper.isMongoAvailable(), "MongoDB not available or socket restricted in sandbox");
        TestMongoHelper.resetDatabase();
        userDAO = new UserDAO();
    }

    @Test
    void testBCryptPasswordHashing() {
        String hash = PasswordUtil.hashPassword("testPass123");
        assertNotNull(hash);
        assertTrue(PasswordUtil.verifyPassword("testPass123", hash));
        assertFalse(PasswordUtil.verifyPassword("wrongPass", hash));
    }

    @Test
    void testAdminAndTeacherAndStudentLogin() {
        User admin = userDAO.authenticate("admin", "admin123");
        assertNotNull(admin);
        assertTrue(admin.isAdmin());

        User teacher = userDAO.authenticate("teacher1", "teacher123");
        assertNotNull(teacher);
        assertTrue(teacher.isTeacher());

        User student = userDAO.authenticate("student1", "student123");
        assertNotNull(student);
        assertTrue(student.isStudent());
    }

    @Test
    void testInvalidLoginRejected() {
        assertNull(userDAO.authenticate("admin", "wrong-password"));
        assertNull(userDAO.authenticate("nonexistent", "admin123"));
    }
}
