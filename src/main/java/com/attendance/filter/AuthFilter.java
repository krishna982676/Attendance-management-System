package com.attendance.filter;

import com.attendance.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Enforces session-based authentication and role-based access control across routes.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Prevent caching of protected pages after logout
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            res.sendRedirect(contextPath + "/login");
            return;
        }

        if (!isAuthorized(user, path, req.getParameter("action"))) {
            req.setAttribute("errorTitle", "Access Denied");
            req.setAttribute("errorMessage", "You do not have permission to access this resource.");
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, res);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("")
                || path.equals("/")
                || path.equals("/index.jsp")
                || path.equals("/login")
                || path.equals("/login.jsp")
                || path.equals("/logout")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }

    private boolean isAuthorized(User user, String path, String action) {
        if (path.startsWith("/dashboard")) {
            return true;
        }
        if (path.startsWith("/teachers") || path.startsWith("/classes")) {
            return user.isAdmin();
        }
        if (path.startsWith("/students")) {
            if (user.isAdmin()) {
                return true;
            }
            // Teachers may view/search students, but only Admins may create/update/delete
            return user.isTeacher() && (action == null || "list".equals(action));
        }
        if (path.startsWith("/attendance")) {
            return user.isAdmin() || user.isTeacher();
        }
        if (path.startsWith("/reports")) {
            return user.isAdmin() || user.isTeacher() || user.isStudent();
        }
        return true;
    }
}

