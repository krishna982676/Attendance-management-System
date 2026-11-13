package com.attendance.controller;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.model.Attendance;
import com.attendance.model.AttendanceReportRow;
import com.attendance.model.SchoolClass;
import com.attendance.model.Student;
import com.attendance.model.Subject;
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

/**
 * Routes authenticated users to their role-specific dashboard (Admin, Teacher, or Student) using MongoDB.
 */
@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            if (user.isAdmin()) {
                loadAdminDashboard(req);
                req.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp").forward(req, resp);
            } else if (user.isTeacher()) {
                loadTeacherDashboard(req, user);
                req.getRequestDispatcher("/WEB-INF/views/teacher-dashboard.jsp").forward(req, resp);
            } else {
                loadStudentDashboard(req, user);
                req.getRequestDispatcher("/WEB-INF/views/student-dashboard.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Dashboard Error");
            req.setAttribute("errorMessage", "Unable to load dashboard data: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private void loadAdminDashboard(HttpServletRequest req) {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        int totalStudents = studentDAO.countStudents();
        int totalTeachers = teacherDAO.countTeachers();
        int totalClasses = classDAO.countClasses();
        int[] todayStats = attendanceDAO.getDateSummary(todayStr);

        double todayRate = todayStats[0] > 0
                ? Math.round(((todayStats[1] * 100.0) / todayStats[0]) * 100.0) / 100.0
                : 0.0;

        List<AttendanceReportRow> overallRows = attendanceDAO.getOverallReport(null, null);
        long belowThresholdCount = overallRows.stream().filter(AttendanceReportRow::isBelowThreshold).count();
        List<Attendance> recentAttendance = attendanceDAO.getAttendanceHistory(null, null, null, null, null, 8);

        req.setAttribute("totalStudents", totalStudents);
        req.setAttribute("totalTeachers", totalTeachers);
        req.setAttribute("totalClasses", totalClasses);
        req.setAttribute("todayTotal", todayStats[0]);
        req.setAttribute("todayPresent", todayStats[1]);
        req.setAttribute("todayAbsent", todayStats[2]);
        req.setAttribute("todayRate", todayRate);
        req.setAttribute("belowThresholdCount", belowThresholdCount);
        req.setAttribute("recentAttendance", recentAttendance);
        req.setAttribute("todayDate", todayStr);
    }

    private void loadTeacherDashboard(HttpServletRequest req, User user) {
        Teacher teacher = teacherDAO.findByUserId(user.getUserId());
        if (teacher == null) {
            req.setAttribute("assignedClasses", Collections.emptyList());
            req.setAttribute("subjects", Collections.emptyList());
            req.setAttribute("classStudents", Collections.emptyList());
            req.setAttribute("recentHistory", Collections.emptyList());
            req.setAttribute("classReport", Collections.emptyList());
            return;
        }

        List<SchoolClass> assignedClasses = classDAO.findByTeacherId(teacher.getTeacherId());
        List<Subject> subjects = subjectDAO.findByTeacherId(teacher.getTeacherId());

        String selectedClassId = ValidationUtil.trim(req.getParameter("classId"));
        if ((selectedClassId.isEmpty() || "null".equals(selectedClassId)) && !assignedClasses.isEmpty()) {
            selectedClassId = assignedClasses.get(0).getClassId();
        }

        List<Student> classStudents = (!selectedClassId.isEmpty())
                ? studentDAO.findByClassId(selectedClassId)
                : Collections.emptyList();
        List<Attendance> recentHistory = (!selectedClassId.isEmpty())
                ? attendanceDAO.getAttendanceHistory(selectedClassId, null, null, null, null, 10)
                : Collections.emptyList();
        List<AttendanceReportRow> classReport = (!selectedClassId.isEmpty())
                ? attendanceDAO.getOverallReport(selectedClassId, null)
                : Collections.emptyList();

        req.setAttribute("teacher", teacher);
        req.setAttribute("assignedClasses", assignedClasses);
        req.setAttribute("subjects", subjects);
        req.setAttribute("selectedClassId", selectedClassId);
        req.setAttribute("classStudents", classStudents);
        req.setAttribute("recentHistory", recentHistory);
        req.setAttribute("classReport", classReport);
    }

    private void loadStudentDashboard(HttpServletRequest req, User user) {
        Student student = studentDAO.findByUserId(user.getUserId());
        if (student == null) {
            req.setAttribute("missingProfile", true);
            return;
        }

        LocalDate now = LocalDate.now();
        int year = ValidationUtil.parseIntOrDefault(req.getParameter("year"), now.getYear());
        int month = ValidationUtil.parseIntOrDefault(req.getParameter("month"), now.getMonthValue());
        String viewMode = ValidationUtil.trim(req.getParameter("viewMode"));
        if (viewMode.isEmpty()) {
            viewMode = "monthly";
        }

        AttendanceReportRow summary = attendanceDAO.getStudentOverallSummary(student.getStudentId());
        List<Attendance> history = "all".equalsIgnoreCase(viewMode)
                ? attendanceDAO.getStudentMonthlyHistory(student.getStudentId(), null, null)
                : attendanceDAO.getStudentMonthlyHistory(student.getStudentId(), year, month);

        req.setAttribute("student", student);
        req.setAttribute("summary", summary);
        req.setAttribute("history", history);
        req.setAttribute("selectedYear", year);
        req.setAttribute("selectedMonth", month);
        req.setAttribute("viewMode", viewMode);
    }
}
