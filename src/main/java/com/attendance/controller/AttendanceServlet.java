package com.attendance.controller;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.ClassDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.model.Attendance;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core controller for marking daily attendance, preventing duplicates,
 * updating existing records, and viewing attendance history using MongoDB.
 */
@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ValidationUtil.trim(req.getParameter("action"));
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");

        try {
            List<SchoolClass> accessibleClasses = getAccessibleClasses(user);
            req.setAttribute("classes", accessibleClasses);

            if ("history".equals(action)) {
                handleHistoryView(req, resp, user, accessibleClasses);
                return;
            }

            // Default: Mark / Update Attendance View
            String selectedClassId = ValidationUtil.trim(req.getParameter("classId"));
            if (selectedClassId.isEmpty() && !accessibleClasses.isEmpty()) {
                selectedClassId = accessibleClasses.get(0).getClassId();
            }

            if (!selectedClassId.isEmpty() && !isClassAccessible(user, accessibleClasses, selectedClassId)) {
                session.setAttribute("flashError", "You are not assigned to the selected class.");
                selectedClassId = accessibleClasses.isEmpty() ? "" : accessibleClasses.get(0).getClassId();
            }

            String selectedSubjectId = ValidationUtil.trim(req.getParameter("subjectId"));
            String selectedDate = ValidationUtil.trim(req.getParameter("date"));
            if (selectedDate.isEmpty()) {
                selectedDate = LocalDate.now().toString();
            }

            List<Student> students = Collections.emptyList();
            List<Subject> subjects = Collections.emptyList();
            Map<String, Attendance> existingMap = Collections.emptyMap();
            boolean alreadyMarked = false;

            if (!selectedClassId.isEmpty()) {
                students = studentDAO.findByClassId(selectedClassId);
                subjects = subjectDAO.findByClassId(selectedClassId);
                existingMap = attendanceDAO.getAttendanceMapForClassSubjectAndDate(
                        selectedClassId, selectedSubjectId.isEmpty() ? null : selectedSubjectId, selectedDate);
                alreadyMarked = !existingMap.isEmpty();
            }

            req.setAttribute("selectedClassId", selectedClassId);
            req.setAttribute("selectedSubjectId", selectedSubjectId);
            req.setAttribute("selectedDate", selectedDate);
            req.setAttribute("students", students);
            req.setAttribute("subjects", subjects);
            req.setAttribute("existingMap", existingMap);
            req.setAttribute("alreadyMarked", alreadyMarked);
            req.getRequestDispatcher("/WEB-INF/views/attendance-mark.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Attendance Error");
            req.setAttribute("errorMessage", "Unable to load attendance module: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ValidationUtil.trim(req.getParameter("action"));
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");

        try {
            if ("updateSingle".equals(action)) {
                handleSingleUpdate(req, resp, session, user);
                return;
            }

            String classId = ValidationUtil.trim(req.getParameter("classId"));
            String subjectId = ValidationUtil.trim(req.getParameter("subjectId"));
            String attendanceDate = ValidationUtil.trim(req.getParameter("attendanceDate"));
            String mode = ValidationUtil.trim(req.getParameter("mode"));
            boolean allowUpdate = "update".equalsIgnoreCase(mode);

            if (classId.isEmpty() || attendanceDate.isEmpty()) {
                session.setAttribute("flashError", "Please select a valid class and date.");
                resp.sendRedirect(req.getContextPath() + "/attendance");
                return;
            }

            List<SchoolClass> accessibleClasses = getAccessibleClasses(user);
            if (!isClassAccessible(user, accessibleClasses, classId)) {
                session.setAttribute("flashError", "You are not authorized to mark attendance for this class.");
                resp.sendRedirect(req.getContextPath() + "/attendance");
                return;
            }

            String[] studentIds = req.getParameterValues("studentId");
            if (studentIds == null || studentIds.length == 0) {
                session.setAttribute("flashError", "No students found to mark attendance.");
                resp.sendRedirect(req.getContextPath() + "/attendance?classId=" + classId + "&date=" + attendanceDate);
                return;
            }

            Map<String, String> statusMap = new LinkedHashMap<>();
            Map<String, String> remarksMap = new LinkedHashMap<>();
            for (String sid : studentIds) {
                String cleanSid = ValidationUtil.trim(sid);
                if (!cleanSid.isEmpty()) {
                    String status = ValidationUtil.trim(req.getParameter("status_" + cleanSid));
                    if (!"Present".equalsIgnoreCase(status) && !"Absent".equalsIgnoreCase(status) && !"Holiday".equalsIgnoreCase(status)) {
                        status = "Present";
                    }
                    String remarks = ValidationUtil.trim(req.getParameter("remarks_" + cleanSid));
                    statusMap.put(cleanSid, status);
                    remarksMap.put(cleanSid, remarks);
                }
            }

            int count = attendanceDAO.saveClassAttendance(
                    classId, subjectId.isEmpty() ? null : subjectId, attendanceDate, user.getUserId(), statusMap, remarksMap, allowUpdate);

            session.setAttribute("flashSuccess",
                    (allowUpdate ? "Updated" : "Saved") + " attendance for " + count + " student(s) on " + attendanceDate + ".");
            resp.sendRedirect(req.getContextPath() + "/attendance?classId=" + classId + "&date=" + attendanceDate
                    + (!subjectId.isEmpty() ? "&subjectId=" + subjectId : ""));
        } catch (AttendanceDAO.DuplicateAttendanceException dupEx) {
            session.setAttribute("flashError", dupEx.getMessage());
            String classId = ValidationUtil.trim(req.getParameter("classId"));
            String date = ValidationUtil.trim(req.getParameter("attendanceDate"));
            String subjectId = ValidationUtil.trim(req.getParameter("subjectId"));
            resp.sendRedirect(req.getContextPath() + "/attendance?classId=" + classId + "&date=" + date
                    + (!subjectId.isEmpty() ? "&subjectId=" + subjectId : ""));
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Attendance Save Error");
            req.setAttribute("errorMessage", "Unable to save attendance: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private void handleSingleUpdate(HttpServletRequest req, HttpServletResponse resp, HttpSession session, User user)
            throws IOException {
        String attendanceId = ValidationUtil.trim(req.getParameter("attendanceId"));
        String status = ValidationUtil.trim(req.getParameter("status"));
        String remarks = ValidationUtil.trim(req.getParameter("remarks"));

        if (attendanceId.isEmpty()) {
            session.setAttribute("flashError", "Invalid attendance record selected.");
            resp.sendRedirect(req.getContextPath() + "/attendance?action=history");
            return;
        }

        Attendance existing = attendanceDAO.findById(attendanceId);
        if (existing == null) {
            session.setAttribute("flashError", "Attendance record not found.");
            resp.sendRedirect(req.getContextPath() + "/attendance?action=history");
            return;
        }

        List<SchoolClass> accessibleClasses = getAccessibleClasses(user);
        if (!isClassAccessible(user, accessibleClasses, existing.getClassId())) {
            session.setAttribute("flashError", "You are not authorized to update attendance for this class.");
            resp.sendRedirect(req.getContextPath() + "/attendance?action=history");
            return;
        }

        attendanceDAO.updateSingleAttendance(attendanceId, status, remarks, user.getUserId());
        session.setAttribute("flashSuccess", "Attendance record updated to '" + status + "'.");
        resp.sendRedirect(req.getContextPath() + "/attendance?action=history&classId=" + existing.getClassId());
    }

    private void handleHistoryView(HttpServletRequest req, HttpServletResponse resp, User user,
                                   List<SchoolClass> accessibleClasses) throws ServletException, IOException {
        String classId = ValidationUtil.trim(req.getParameter("classId"));
        if (user.isTeacher() && classId.isEmpty() && !accessibleClasses.isEmpty()) {
            classId = accessibleClasses.get(0).getClassId();
        }
        if (!classId.isEmpty() && !isClassAccessible(user, accessibleClasses, classId)) {
            classId = accessibleClasses.isEmpty() ? "" : accessibleClasses.get(0).getClassId();
        }

        String studentId = ValidationUtil.trim(req.getParameter("studentId"));
        String fromDate = ValidationUtil.trim(req.getParameter("fromDate"));
        String toDate = ValidationUtil.trim(req.getParameter("toDate"));

        List<Attendance> history = attendanceDAO.getAttendanceHistory(
                classId.isEmpty() ? null : classId,
                null,
                studentId.isEmpty() ? null : studentId,
                fromDate.isEmpty() ? null : fromDate,
                toDate.isEmpty() ? null : toDate,
                200);

        List<Student> students = (!classId.isEmpty())
                ? studentDAO.findByClassId(classId)
                : studentDAO.findAll(null, null);

        req.setAttribute("history", history);
        req.setAttribute("students", students);
        req.setAttribute("selectedClassId", classId);
        req.setAttribute("selectedStudentId", studentId);
        req.setAttribute("fromDate", fromDate);
        req.setAttribute("toDate", toDate);
        req.getRequestDispatcher("/WEB-INF/views/attendance-history.jsp").forward(req, resp);
    }

    private List<SchoolClass> getAccessibleClasses(User user) {
        if (user.isAdmin()) {
            return classDAO.findAll();
        }
        Teacher t = teacherDAO.findByUserId(user.getUserId());
        return (t != null) ? classDAO.findByTeacherId(t.getTeacherId()) : Collections.emptyList();
    }

    private boolean isClassAccessible(User user, List<SchoolClass> accessibleClasses, String classId) {
        if (user.isAdmin()) {
            return true;
        }
        return accessibleClasses.stream().anyMatch(c -> c.getClassId().equals(classId));
    }
}
