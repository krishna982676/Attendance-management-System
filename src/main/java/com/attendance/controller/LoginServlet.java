package com.attendance.controller;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import com.attendance.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles user login and session creation for Admin, Teacher, and Student roles using MongoDB.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO;

    public LoginServlet() {
        this.userDAO = new UserDAO();
    }

    public LoginServlet(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession existing = req.getSession(false);
        if (existing != null && existing.getAttribute("loggedInUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = ValidationUtil.trim(req.getParameter("username"));
        String password = req.getParameter("password");

        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            req.setAttribute("error", "Please enter both username and password.");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                req.setAttribute("error", "Invalid username or password.");
                req.setAttribute("username", username);
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            // Prevent session fixation by creating a fresh session on login
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("loggedInUser", user);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (Exception e) {
            req.setAttribute("error", "Database connection error. Please ensure MongoDB is running on localhost:27017.");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
