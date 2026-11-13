package com.attendance.controller;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.model.Attendance;
import com.attendance.model.AttendanceReportRow;
import com.attendance.model.SchoolClass;
import com.attendance.model.Student;
import com.attendance.model.Teacher;
import com.attendance.model.User;
import com.attendance.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates Daily, Monthly, and Overall Attendance Reports with <75% shortage highlighting using MongoDB.
 */
@WebServlet("/reports")
public class ReportServlet extends HttpServlet {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");

        try {
            String reportType = ValidationUtil.trim(req.getParameter("reportType"));
            if (reportType.isEmpty()) {
                reportType = "monthly";
            }

            LocalDate now = LocalDate.now();
            String dailyDate = ValidationUtil.trim(req.getParameter("date"));
            if (dailyDate.isEmpty()) {
                dailyDate = now.toString();
            }
            int year = ValidationUtil.parseIntOrDefault(req.getParameter("year"), now.getYear());
            int month = ValidationUtil.parseIntOrDefault(req.getParameter("month"), now.getMonthValue());
            boolean onlyLow = "true".equalsIgnoreCase(req.getParameter("onlyLow"));

            String classId = ValidationUtil.trim(req.getParameter("classId"));
            String studentId = ValidationUtil.trim(req.getParameter("studentId"));

            List<SchoolClass> classes;
            List<Student> students;

            if (user.isStudent()) {
                Student self = studentDAO.findByUserId(user.getUserId());
                if (self != null) {
                    studentId = self.getStudentId();
                    classId = self.getClassId() != null ? self.getClassId() : "";
                    students = Collections.singletonList(self);
                } else {
                    students = Collections.emptyList();
                }
                classes = Collections.emptyList();
            } else if (user.isTeacher()) {
                Teacher t = teacherDAO.findByUserId(user.getUserId());
                classes = (t != null) ? classDAO.findByTeacherId(t.getTeacherId()) : Collections.emptyList();
                if (classId.isEmpty() && !classes.isEmpty()) {
                    classId = classes.get(0).getClassId();
                }
                students = (!classId.isEmpty()) ? studentDAO.findByClassId(classId) : Collections.emptyList();
            } else {
                classes = classDAO.findAll();
                students = (!classId.isEmpty()) ? studentDAO.findByClassId(classId) : studentDAO.findAll(null, null);
            }

            if ("daily".equalsIgnoreCase(reportType)) {
                List<Attendance> dailyRecords = attendanceDAO.getDailyReport(dailyDate, classId.isEmpty() ? null : classId);
                if (!studentId.isEmpty()) {
                    final String targetStudentId = studentId;
                    dailyRecords = dailyRecords.stream()
                            .filter(a -> targetStudentId.equals(a.getStudentId()))
                            .collect(Collectors.toList());
                }
                long presentCount = dailyRecords.stream().filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
                long absentCount = dailyRecords.stream().filter(a -> "Absent".equalsIgnoreCase(a.getStatus())).count();
                req.setAttribute("dailyRecords", dailyRecords);
                req.setAttribute("presentCount", presentCount);
                req.setAttribute("absentCount", absentCount);
            } else {
                List<AttendanceReportRow> reportRows = "overall".equalsIgnoreCase(reportType)
                        ? attendanceDAO.getOverallReport(classId.isEmpty() ? null : classId, studentId.isEmpty() ? null : studentId)
                        : attendanceDAO.getMonthlyReport(year, month, classId.isEmpty() ? null : classId, studentId.isEmpty() ? null : studentId);

                long lowAttendanceCount = reportRows.stream().filter(AttendanceReportRow::isBelowThreshold).count();
                if (onlyLow) {
                    reportRows = reportRows.stream()
                            .filter(AttendanceReportRow::isBelowThreshold)
                            .collect(Collectors.toList());
                }
                req.setAttribute("reportRows", reportRows);
                req.setAttribute("lowAttendanceCount", lowAttendanceCount);
            }

            req.setAttribute("reportType", reportType);
            req.setAttribute("classes", classes);
            req.setAttribute("students", students);
            req.setAttribute("selectedClassId", classId);
            req.setAttribute("selectedStudentId", studentId);
            req.setAttribute("selectedDate", dailyDate);
            req.setAttribute("selectedYear", year);
            req.setAttribute("selectedMonth", month);
            req.setAttribute("onlyLow", onlyLow);

            req.getRequestDispatcher("/WEB-INF/views/reports.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Report Generation Error");
            req.setAttribute("errorMessage", "Unable to generate report: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }
}
