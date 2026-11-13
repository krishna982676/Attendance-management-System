package com.attendance.controller;

import com.attendance.dao.ClassDAO;
import com.attendance.dao.SubjectDAO;
import com.attendance.dao.TeacherDAO;
import com.attendance.model.SchoolClass;
import com.attendance.model.Subject;
import com.attendance.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Manages Classes and Subjects (Admin only) using MongoDB.
 */
@WebServlet("/classes")
public class ClassServlet extends HttpServlet {

    private final ClassDAO classDAO = new ClassDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String editClassId = ValidationUtil.trim(req.getParameter("editClassId"));
            if (!editClassId.isEmpty()) {
                req.setAttribute("editClass", classDAO.findById(editClassId));
            }
            req.setAttribute("classes", classDAO.findAll());
            req.setAttribute("subjects", subjectDAO.findAll());
            req.setAttribute("teachers", teacherDAO.findAll(null));
            req.getRequestDispatcher("/WEB-INF/views/classes.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorTitle", "Classes & Subjects Error");
            req.setAttribute("errorMessage", "Unable to load classes and subjects: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ValidationUtil.trim(req.getParameter("action"));
        HttpSession session = req.getSession(false);

        try {
            switch (action) {
                case "createClass" -> {
                    SchoolClass sc = extractClass(req);
                    if (ValidationUtil.isBlank(sc.getClassName()) || ValidationUtil.isBlank(sc.getSection()) || ValidationUtil.isBlank(sc.getDepartment())) {
                        session.setAttribute("flashError", "Class name, section, and department are required.");
                    } else {
                        classDAO.createClass(sc);
                        session.setAttribute("flashSuccess", "Class '" + sc.getDisplayName() + "' added successfully.");
                    }
                }
                case "updateClass" -> {
                    SchoolClass sc = extractClass(req);
                    String classId = ValidationUtil.trim(req.getParameter("classId"));
                    if (classId.isEmpty() || ValidationUtil.isBlank(sc.getClassName()) || ValidationUtil.isBlank(sc.getSection())) {
                        session.setAttribute("flashError", "Invalid class details provided.");
                    } else {
                        sc.setClassId(classId);
                        classDAO.updateClass(sc);
                        session.setAttribute("flashSuccess", "Class updated successfully.");
                    }
                }
                case "deleteClass" -> {
                    String classId = ValidationUtil.trim(req.getParameter("classId"));
                    if (!classId.isEmpty() && classDAO.deleteClass(classId)) {
                        session.setAttribute("flashSuccess", "Class deleted successfully.");
                    } else {
                        session.setAttribute("flashError", "Unable to delete class.");
                    }
                }
                case "createSubject" -> {
                    Subject sub = new Subject();
                    sub.setSubjectCode(ValidationUtil.trim(req.getParameter("subjectCode")).toUpperCase());
                    sub.setSubjectName(ValidationUtil.trim(req.getParameter("subjectName")));
                    String classId = ValidationUtil.trim(req.getParameter("classId"));
                    String teacherId = ValidationUtil.trim(req.getParameter("teacherId"));
                    sub.setTeacherId(teacherId.isEmpty() ? null : teacherId);

                    if (ValidationUtil.isBlank(sub.getSubjectCode()) || ValidationUtil.isBlank(sub.getSubjectName()) || classId.isEmpty()) {
                        session.setAttribute("flashError", "Subject code, subject name, and class are required.");
                    } else {
                        sub.setClassId(classId);
                        subjectDAO.createSubject(sub);
                        session.setAttribute("flashSuccess", "Subject '" + sub.getSubjectName() + "' added successfully.");
                    }
                }
                case "deleteSubject" -> {
                    String subjectId = ValidationUtil.trim(req.getParameter("subjectId"));
                    if (!subjectId.isEmpty() && subjectDAO.deleteSubject(subjectId)) {
                        session.setAttribute("flashSuccess", "Subject deleted successfully.");
                    } else {
                        session.setAttribute("flashError", "Unable to delete subject.");
                    }
                }
                default -> session.setAttribute("flashError", "Unknown action.");
            }
        } catch (Exception e) {
            session.setAttribute("flashError", "Error: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/classes");
    }

    private SchoolClass extractClass(HttpServletRequest req) {
        SchoolClass sc = new SchoolClass();
        sc.setClassName(ValidationUtil.trim(req.getParameter("className")));
        sc.setSection(ValidationUtil.trim(req.getParameter("section")).toUpperCase());
        sc.setDepartment(ValidationUtil.trim(req.getParameter("department")));
        String year = ValidationUtil.trim(req.getParameter("academicYear"));
        sc.setAcademicYear(year.isEmpty() ? "2026-2027" : year);
        return sc;
    }
}
