package com.attendance.controller;

import com.attendance.dao.ClassDAO;
import com.attendance.dao.StudentDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.dao.UserDAO;
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
import java.util.List;

/**
 * Handles Student CRUD operations, search, and class assignment using MongoDB.
 */
@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ClassDAO classDAO = new ClassDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ValidationUtil.trim(req.getParameter("action"));
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");

        try {
            if ("new".equals(action) && user.isAdmin()) {
                req.setAttribute("classes", classDAO.findAll());
                req.setAttribute("student", new Student());
                req.setAttribute("formMode", "create");
                req.getRequestDispatcher("/WEB-INF/views/student-form.jsp").forward(req, resp);
                return;
            }

            if ("edit".equals(action) && user.isAdmin()) {
                String id = ValidationUtil.trim(req.getParameter("id"));
                if (id.isEmpty()) {
                    resp.sendRedirect(req.getContextPath() + "/students");
                    return;
                }
                Student student = studentDAO.findById(id);
                if (student == null) {
                    session.setAttribute("flashError", "Student record not found.");
                    resp.sendRedirect(req.getContextPath() + "/students");
                    return;
                }
                req.setAttribute("classes", classDAO.findAll());
                req.setAttribute("student", student);
                req.setAttribute("formMode", "update");
                req.getRequestDispatcher("/WEB-INF/views/student-form.jsp").forward(req, resp);
                return;
            }

            // List / search view
            String search = ValidationUtil.trim(req.getParameter("q"));
            String classId = ValidationUtil.trim(req.getParameter("classId"));

            List<SchoolClass> classes;
            if (user.isTeacher()) {
                Teacher t = teacherDAO.findByUserId(user.getUserId());
                classes = (t != null) ? classDAO.findByTeacherId(t.getTeacherId()) : classDAO.findAll();
            } else {
                classes = classDAO.findAll();
            }

            List<Student> students = studentDAO.findAll(search, classId.isEmpty() ? null : classId);
            req.setAttribute("students", students);
            req.setAttribute("classes", classes);
            req.setAttribute("searchQuery", search);
            req.setAttribute("selectedClassId", classId);
            req.getRequestDispatcher("/WEB-INF/views/students.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Student Management Error");
            req.setAttribute("errorMessage", "Unable to process student request: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User user = (User) session.getAttribute("loggedInUser");
        if (!user.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = ValidationUtil.trim(req.getParameter("action"));

        try {
            if ("delete".equals(action)) {
                String id = ValidationUtil.trim(req.getParameter("id"));
                if (!id.isEmpty() && studentDAO.deleteStudent(id)) {
                    session.setAttribute("flashSuccess", "Student record deleted successfully.");
                } else {
                    session.setAttribute("flashError", "Unable to delete student record.");
                }
                resp.sendRedirect(req.getContextPath() + "/students");
                return;
            }

            Student student = new Student();
            String studentId = ValidationUtil.trim(req.getParameter("studentId"));
            if (!studentId.isEmpty()) {
                student.setStudentId(studentId);
            }
            student.setName(ValidationUtil.trim(req.getParameter("name")));
            student.setRollNumber(ValidationUtil.trim(req.getParameter("rollNumber")).toUpperCase());
            student.setEmail(ValidationUtil.trim(req.getParameter("email")).toLowerCase());
            student.setPhone(ValidationUtil.trim(req.getParameter("phone")));
            student.setClassId(ValidationUtil.trim(req.getParameter("classId")));
            student.setDepartment(ValidationUtil.trim(req.getParameter("department")));

            String loginUsername = ValidationUtil.trim(req.getParameter("loginUsername"));
            String loginPassword = req.getParameter("loginPassword");
            boolean isUpdate = "update".equals(action);

            String validationError = validateStudent(student, isUpdate ? student.getStudentId() : null,
                    isUpdate ? null : loginUsername, isUpdate ? null : loginPassword);
            if (validationError != null) {
                req.setAttribute("error", validationError);
                req.setAttribute("student", student);
                req.setAttribute("classes", classDAO.findAll());
                req.setAttribute("formMode", isUpdate ? "update" : "create");
                req.setAttribute("loginUsername", loginUsername);
                req.getRequestDispatcher("/WEB-INF/views/student-form.jsp").forward(req, resp);
                return;
            }

            if (isUpdate) {
                studentDAO.updateStudent(student);
                session.setAttribute("flashSuccess", "Student '" + student.getName() + "' updated successfully.");
            } else {
                studentDAO.createStudent(student, loginUsername, loginPassword);
                session.setAttribute("flashSuccess", "Student '" + student.getName() + "' added successfully.");
            }
            resp.sendRedirect(req.getContextPath() + "/students");
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Student Save Error");
            req.setAttribute("errorMessage", "Unable to save student record: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    private String validateStudent(Student s, String excludeId, String loginUsername, String loginPassword) {
        if (ValidationUtil.isBlank(s.getName())) {
            return "Student name is required.";
        }
        if (ValidationUtil.isBlank(s.getRollNumber())) {
            return "Roll number is required.";
        }
        if (!ValidationUtil.isValidEmail(s.getEmail())) {
            return "Please enter a valid email address.";
        }
        if (!ValidationUtil.isValidPhone(s.getPhone())) {
            return "Please enter a valid phone number (7-20 digits).";
        }
        if (ValidationUtil.isBlank(s.getDepartment())) {
            return "Department is required.";
        }
        if (ValidationUtil.isBlank(s.getClassId())) {
            return "Please assign the student to a class.";
        }
        if (studentDAO.rollNumberExists(s.getRollNumber(), excludeId)) {
            return "A student with Roll Number '" + s.getRollNumber() + "' already exists.";
        }
        if (studentDAO.emailExists(s.getEmail(), excludeId)) {
            return "A student with email '" + s.getEmail() + "' already exists.";
        }
        if (excludeId == null && !ValidationUtil.isBlank(loginUsername)) {
            if (ValidationUtil.isBlank(loginPassword) || loginPassword.length() < 6) {
                return "Student portal password must be at least 6 characters when creating a login account.";
            }
            if (userDAO.usernameExists(loginUsername, null)) {
                return "Username '" + loginUsername + "' is already taken.";
            }
        }
        return null;
    }
}
