package com.attendance.util;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void testBlankAndTrim() {
        assertTrue(ValidationUtil.isBlank(null));
        assertTrue(ValidationUtil.isBlank(""));
        assertTrue(ValidationUtil.isBlank("   "));
        assertFalse(ValidationUtil.isBlank("hello"));
        assertEquals("abc", ValidationUtil.trim("  abc  "));
    }

    @Test
    void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("admin@college.edu"));
        assertTrue(ValidationUtil.isValidEmail("student.1@domain.co.in"));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@nodomain.com"));
    }

    @Test
    void testPhoneValidation() {
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("+91-9876543210"));
        assertFalse(ValidationUtil.isValidPhone("123"));
    }

    @Test
    void testObjectIdValidation() {
        ObjectId oid = new ObjectId();
        assertTrue(ValidationUtil.isValidObjectId(oid.toHexString()));
        assertFalse(ValidationUtil.isValidObjectId("invalid-oid"));
        assertNotNull(ValidationUtil.parseObjectIdOrNull(oid.toHexString()));
        assertNull(ValidationUtil.parseObjectIdOrNull("invalid"));
    }

    @Test
    void testDateParsing() {
        LocalDate d = ValidationUtil.parseLocalDateOrNull("2026-11-13");
        assertNotNull(d);
        assertEquals(2026, d.getYear());
        assertEquals(11, d.getMonthValue());
        assertEquals(13, d.getDayOfMonth());
        assertNull(ValidationUtil.parseLocalDateOrNull("invalid-date"));
    }
}
