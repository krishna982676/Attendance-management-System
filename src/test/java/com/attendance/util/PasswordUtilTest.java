package com.attendance.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void testBCryptHashAndVerify() {
        String plain = "admin123";
        String hash = PasswordUtil.hashPassword(plain);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
        assertTrue(PasswordUtil.verifyPassword("admin123", hash));
        assertFalse(PasswordUtil.verifyPassword("wrongpass", hash));
        assertFalse(PasswordUtil.verifyPassword(null, hash));
        assertFalse(PasswordUtil.verifyPassword("admin123", null));
    }

    @Test
    void testEmptyPasswordThrows() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
    }
}
