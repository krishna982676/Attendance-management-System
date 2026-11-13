package com.attendance.controller;

import com.attendance.dao.ClassDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.dao.UserDAO;
import com.attendance.model.Teacher;
import com.attendance.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles Teacher CRUD operations and class assignments (Admin only) using MongoDB.
 */
@WebServlet("/teachers")
public class TeacherServlet extends HttpServlet {

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ValidationUtil.trim(req.getParameter("action"));

        try {
            if ("new".equals(action)) {
                req.setAttribute("classes", classDAO.findAll());
                req.setAttribute("teacher", new Teacher());
                req.setAttribute("formMode", "create");
                req.getRequestDispatcher("/WEB-INF/views/teacher-form.jsp").forward(req, resp);
                return;
            }

            if ("edit".equals(action)) {
                String id = ValidationUtil.trim(req.getParameter("id"));
                if (id.isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/teachers");
                    return;
                }
                Teacher teacher = teacherDAO.findById(id);
                if (teacher == null) {
                    req.getSession().setAttribute("flashError", "Teacher record not found.");
                    resp.sendRedirect(req.getContextPath() + "/teachers");
                    return;
                }
                req.setAttribute("classes", classDAO.findAll());
                req.setAttribute("teacher", teacher);
                req.setAttribute("formMode", "update");
                req.getRequestDispatcher("/WEB-INF/views/teacher-form.jsp").forward(req, resp);
                return;
            }

            String search = ValidationUtil.trim(req.getParameter("q"));
            req.setAttribute("teachers", teacherDAO.findAll(search));
            req.setAttribute("searchQuery", search);
            req.getRequestDispatcher("/WEB-INF/views/teachers.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Teacher Management Error");
            req.setAttribute("errorMessage", "Unable to load teachers: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ValidationUtil.trim(req.getParameter("action"));
        HttpSession session = req.getSession(false);

        try {
            if ("delete".equals(action)) {
                String id = ValidationUtil.trim(req.getParameter("id"));
                if (!id.isEmpty() && teacherDAO.deleteTeacher(id)) {
                    session.setAttribute("flashSuccess", "Teacher record deleted successfully.");
                } else {
                    session.setAttribute("flashError", "Unable to delete teacher record.");
                }
                resp.sendRedirect(req.getContextPath() + "/teachers");
                return;
            }

            Teacher teacher = new Teacher();
            String teacherId = ValidationUtil.trim(req.getParameter("teacherId"));
            if (!teacherId.isEmpty()) {
                teacher.setTeacherId(teacherId);
            }
            teacher.setEmployeeId(ValidationUtil.trim(req.getParameter("employeeId")).toUpperCase());
            teacher.setName(ValidationUtil.trim(req.getParameter("name")));
            teacher.setEmail(ValidationUtil.trim(req.getParameter("email")).toLowerCase());
            teacher.setPhone(ValidationUtil.trim(req.getParameter("phone")));
            teacher.setDepartment(ValidationUtil.trim(req.getParameter("department")));
            String classId = ValidationUtil.trim(req.getParameter("assignedClassId"));
            teacher.setAssignedClassId(classId.isEmpty() ? null : classId);

            String loginUsername = ValidationUtil.trim(req.getParameter("loginUsername"));
            String loginPassword = req.getParameter("loginPassword");
            boolean isUpdate = "update".equals(action);

            String error = validateTeacher(teacher, isUpdate ? teacher.getTeacherId() : null,
                    isUpdate ? null : loginUsername, isUpdate ? null : loginPassword);
            if (error != null) {
                req.setAttribute("error", error);
                req.setAttribute("teacher", teacher);
                req.setAttribute("classes", classDAO.findAll());
                req.setAttribute("formMode", isUpdate ? "update" : "create");
                req.setAttribute("loginUsername", loginUsername);
                req.getRequestDispatcher("/WEB-INF/views/teacher-form.jsp").forward(req, resp);
                return;
            }

            if (isUpdate) {
                teacherDAO.updateTeacher(teacher);
                session.setAttribute("flashSuccess", "Teacher '" + teacher.getName() + "' updated successfully.");
            } else {
                teacherDAO.createTeacher(teacher, loginUsername, loginPassword);
                session.setAttribute("flashSuccess", "Teacher '" + teacher.getName() + "' added successfully.");
            }
            resp.sendRedirect(req.getContextPath() + "/teachers");
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Teacher Save Error");
            req.setAttribute("errorMessage", "Unable to save teacher record: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private String validateTeacher(Teacher t, String excludeId, String loginUsername, String loginPassword) {
        if (ValidationUtil.isBlank(t.getEmployeeId())) {
            return "Employee ID is required.";
        }
        if (ValidationUtil.isBlank(t.getName())) {
            return "Teacher name is required.";
        }
        if (!ValidationUtil.isValidEmail(t.getEmail())) {
            return "Please enter a valid email address.";
        }
        if (!ValidationUtil.isValidPhone(t.getPhone())) {
            return "Please enter a valid phone number.";
        }
        if (ValidationUtil.isBlank(t.getDepartment())) {
            return "Department is required.";
        }
        if (teacherDAO.employeeIdExists(t.getEmployeeId(), excludeId)) {
            return "Employee ID '" + t.getEmployeeId() + "' already exists.";
        }
        if (excludeId == null) {
            if (ValidationUtil.isBlank(loginUsername)) {
                return "Login username is required for the teacher account.";
            }
            if (ValidationUtil.isBlank(loginPassword) || loginPassword.length() < 6) {
                return "Password must be at least 6 characters.";
            }
            if (userDAO.usernameExists(loginUsername, null)) {
                return "Username '" + loginUsername + "' is already taken.";
            }
        }
        return null;
    }
}
