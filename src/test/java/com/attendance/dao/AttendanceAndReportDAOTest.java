package com.attendance.dao;

import com.attendance.TestMongoHelper;
import com.attendance.model.Attendance;
import com.attendance.model.AttendanceReportRow;
import com.attendance.model.SchoolClass;
import com.attendance.model.Student;
import com.attendance.model.Subject;
import com.attendance.model.Teacher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceAndReportDAOTest {

    private AttendanceDAO attendanceDAO;
    private ClassDAO classDAO;
    private StudentDAO studentDAO;
    private SubjectDAO subjectDAO;
    private TeacherDAO teacherDAO;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(TestMongoHelper.isMongoAvailable(), "MongoDB not available or socket restricted in sandbox");
        TestMongoHelper.resetDatabase();
        attendanceDAO = new AttendanceDAO();
        classDAO = new ClassDAO();
        studentDAO = new StudentDAO();
        subjectDAO = new SubjectDAO();
        teacherDAO = new TeacherDAO();
    }

    @Test
    void testSaveAttendancePreventDuplicateAndUpdate() throws Exception {
        SchoolClass c = classDAO.findAll().get(0);
        List<Student> students = studentDAO.findByClassId(c.getClassId());
        assertTrue(students.size() >= 2);
        String s1Id = students.get(0).getStudentId();
        String s2Id = students.get(1).getStudentId();
        List<Subject> subjects = subjectDAO.findByClassId(c.getClassId());
        String subId = subjects.isEmpty() ? null : subjects.get(0).getSubjectId();
        Teacher t = teacherDAO.findAll(null).get(0);

        String day1 = "2026-11-10";
        Map<String, String> statuses = new HashMap<>();
        statuses.put(s1Id, "Present");
        statuses.put(s2Id, "Absent");

        Map<String, String> remarks = new HashMap<>();
        remarks.put(s1Id, "On time");
        remarks.put(s2Id, "Sick leave");

        // Initial save
        int saved = attendanceDAO.saveClassAttendance(c.getClassId(), subId, day1, t.getUserId(), statuses, remarks, false);
        assertEquals(2, saved);
        assertTrue(attendanceDAO.hasAttendanceForClassSubjectAndDate(c.getClassId(), subId, day1));

        // Duplicate insert in create mode must throw DuplicateAttendanceException
        assertThrows(AttendanceDAO.DuplicateAttendanceException.class, () ->
                attendanceDAO.saveClassAttendance(c.getClassId(), subId, day1, t.getUserId(), statuses, remarks, false)
        );

        // Authorized update mode succeeds
        statuses.put(s2Id, "Present");
        int updated = attendanceDAO.saveClassAttendance(c.getClassId(), subId, day1, t.getUserId(), statuses, remarks, true);
        assertEquals(2, updated);

        List<Attendance> daily = attendanceDAO.getDailyReport(day1, c.getClassId());
        assertEquals(2, daily.size());
        assertTrue(daily.stream().allMatch(a -> "Present".equals(a.getStatus())));
    }

    @Test
    void testAttendancePercentageAndDailyMonthlyReports() throws Exception {
        SchoolClass c = classDAO.findAll().get(0);
        List<Student> students = studentDAO.findByClassId(c.getClassId());
        String s1Id = students.get(0).getStudentId();
        String s2Id = students.get(1).getStudentId();
        Teacher t = teacherDAO.findAll(null).get(0);

        // Record 4 days: Student 1 present 4/4 (100%), Student 2 present 2/4 (50% < 75%)
        String[] dates = {"2026-11-01", "2026-11-02", "2026-11-03", "2026-11-04"};
        for (int i = 0; i < dates.length; i++) {
            Map<String, String> statuses = new HashMap<>();
            statuses.put(s1Id, "Present");
            statuses.put(s2Id, i < 2 ? "Present" : "Absent");
            attendanceDAO.saveClassAttendance(c.getClassId(), null, dates[i], t.getUserId(), statuses, null, false);
        }

        // Daily report check
        List<Attendance> dailyNov3 = attendanceDAO.getDailyReport("2026-11-03", c.getClassId());
        assertEquals(2, dailyNov3.size());

        // Monthly report & percentage check
        List<AttendanceReportRow> monthly = attendanceDAO.getMonthlyReport(2026, 11, c.getClassId(), null);
        assertEquals(2, monthly.size());

        AttendanceReportRow rohan = monthly.stream().filter(r -> s1Id.equals(r.getStudentId())).findFirst().orElseThrow();
        assertEquals(4, rohan.getTotalDays());
        assertEquals(4, rohan.getPresentDays());
        assertEquals(100.0, rohan.getAttendancePercentage());
        assertFalse(rohan.isBelowThreshold());

        AttendanceReportRow vikram = monthly.stream().filter(r -> s2Id.equals(r.getStudentId())).findFirst().orElseThrow();
        assertEquals(4, vikram.getTotalDays());
        assertEquals(2, vikram.getPresentDays());
        assertEquals(2, vikram.getAbsentDays());
        assertEquals(50.0, vikram.getAttendancePercentage());
        assertTrue(vikram.isBelowThreshold());
    }
}
