package com.attendance.controller;

import com.attendance.filter.AuthFilter;
import com.attendance.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ServletAuthAndFlowTest {

    @Test
    void testLogoutInvalidatesSessionAndRedirects() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        AtomicBoolean invalidated = new AtomicBoolean(false);
        AtomicReference<String> redirectedTo = new AtomicReference<>();

        HttpSession session = createSessionProxy(new HashMap<>(), invalidated);
        HttpServletRequest req = createRequestProxy("/attendance-management", "/attendance-management/logout", Map.of(), session, null);
        HttpServletResponse resp = createResponseProxy(redirectedTo, new AtomicInteger(200));

        servlet.doGet(req, resp);

        assertTrue(invalidated.get());
        assertEquals("/attendance-management/login?loggedOut=true", redirectedTo.get());
    }

    @Test
    void testAuthFilterBlocksUnauthenticatedAndEnforcesRoleAccess() throws Exception {
        AuthFilter filter = new AuthFilter();

        // 1. Unauthenticated request redirected to /login
        AtomicReference<String> unauthRedirect = new AtomicReference<>();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        HttpServletRequest reqUnauth = createRequestProxy("/ams", "/ams/students", Map.of(), null, null);
        HttpServletResponse respUnauth = createResponseProxy(unauthRedirect, new AtomicInteger(200));
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(reqUnauth, respUnauth, chain);
        assertEquals("/ams/login", unauthRedirect.get());
        assertFalse(chainCalled.get());

        // 2. Teacher blocked from /teachers (Admin only)
        User teacher = new User("2", "teacher1", "TEACHER", "Prof. Aarav", "a@college.edu", true);
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("loggedInUser", teacher);
        HttpSession teacherSession = createSessionProxy(sessionAttrs, new AtomicBoolean(false));

        AtomicBoolean forwardedToError = new AtomicBoolean(false);
        AtomicInteger status = new AtomicInteger(200);
        RequestDispatcher rd = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        forwardedToError.set(true);
                    }
                    return null;
                }
        );

        HttpServletRequest reqTeacher = createRequestProxy("/ams", "/ams/teachers", Map.of(), teacherSession, rd);
        HttpServletResponse respTeacher = createResponseProxy(new AtomicReference<>(), status);

        filter.doFilter(reqTeacher, respTeacher, chain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, status.get());
        assertTrue(forwardedToError.get());
        assertFalse(chainCalled.get());

        // 3. Admin allowed through to /teachers
        User admin = new User("1", "admin", "ADMIN", "System Admin", "admin@college.edu", true);
        sessionAttrs.put("loggedInUser", admin);
        HttpServletRequest reqAdmin = createRequestProxy("/ams", "/ams/teachers", Map.of(), teacherSession, rd);
        HttpServletResponse respAdmin = createResponseProxy(new AtomicReference<>(), new AtomicInteger(200));

        filter.doFilter(reqAdmin, respAdmin, chain);
        assertTrue(chainCalled.get());
    }

    private HttpSession createSessionProxy(Map<String, Object> attrs, AtomicBoolean invalidated) {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> attrs.get(args[0]);
                    case "setAttribute" -> {
                        attrs.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "invalidate" -> {
                        invalidated.set(true);
                        attrs.clear();
                        yield null;
                    }
                    default -> null;
                }
        );
    }

    private HttpServletRequest createRequestProxy(String contextPath,
                                                    String requestUri,
                                                    Map<String, String> params,
                                                    HttpSession session,
                                                    RequestDispatcher dispatcher) {
        Map<String, Object> reqAttrs = new HashMap<>();
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getContextPath" -> contextPath;
                    case "getRequestURI" -> requestUri;
                    case "getParameter" -> params.get(args[0]);
                    case "getSession" -> {
                        boolean create = args != null && args.length > 0 && Boolean.TRUE.equals(args[0]);
                        yield (session != null || !create) ? session : createSessionProxy(new HashMap<>(), new AtomicBoolean(false));
                    }
                    case "setAttribute" -> {
                        reqAttrs.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "getAttribute" -> reqAttrs.get(args[0]);
                    case "getRequestDispatcher" -> dispatcher;
                    default -> null;
                }
        );
    }

    private HttpServletResponse createResponseProxy(AtomicReference<String> redirectedTo, AtomicInteger status) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendRedirect" -> {
                        redirectedTo.set((String) args[0]);
                        yield null;
                    }
                    case "setStatus" -> {
                        status.set((Integer) args[0]);
                        yield null;
                    }
                    default -> null;
                }
        );
    }
}
