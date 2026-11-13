<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar no-print" id="appSidebar">
    <div class="sidebar-brand">
        <img src="${pageContext.request.contextPath}/images/logo.svg" alt="Logo">
        <div>
            <div class="sidebar-brand-title">AttendancePro</div>
            <div class="sidebar-brand-sub">Management System</div>
        </div>
    </div>

    <nav class="sidebar-nav">
        <div class="sidebar-section-label">Overview</div>
        <a href="${pageContext.request.contextPath}/dashboard"
           class="sidebar-link ${activeNav == 'dashboard' ? 'active' : ''}">
            <i class="bi bi-speedometer2"></i>
            <span>Dashboard</span>
        </a>

        <c:if test="${sessionScope.loggedInUser.admin}">
            <div class="sidebar-section-label">Administration</div>
            <a href="${pageContext.request.contextPath}/students"
               class="sidebar-link ${activeNav == 'students' ? 'active' : ''}">
                <i class="bi bi-mortarboard"></i>
                <span>Students</span>
            </a>
            <a href="${pageContext.request.contextPath}/teachers"
               class="sidebar-link ${activeNav == 'teachers' ? 'active' : ''}">
                <i class="bi bi-person-badge"></i>
                <span>Teachers</span>
            </a>
            <a href="${pageContext.request.contextPath}/classes"
               class="sidebar-link ${activeNav == 'classes' ? 'active' : ''}">
                <i class="bi bi-building"></i>
                <span>Classes &amp; Subjects</span>
            </a>
        </c:if>

        <c:if test="${sessionScope.loggedInUser.teacher}">
            <div class="sidebar-section-label">Academics</div>
            <a href="${pageContext.request.contextPath}/students"
               class="sidebar-link ${activeNav == 'students' ? 'active' : ''}">
                <i class="bi bi-mortarboard"></i>
                <span>Class Students</span>
            </a>
        </c:if>

        <c:if test="${sessionScope.loggedInUser.admin || sessionScope.loggedInUser.teacher}">
            <div class="sidebar-section-label">Attendance</div>
            <a href="${pageContext.request.contextPath}/attendance"
               class="sidebar-link ${activeNav == 'attendance-mark' ? 'active' : ''}">
                <i class="bi bi-calendar-check"></i>
                <span>Mark Attendance</span>
            </a>
            <a href="${pageContext.request.contextPath}/attendance?action=history"
               class="sidebar-link ${activeNav == 'attendance-history' ? 'active' : ''}">
                <i class="bi bi-clock-history"></i>
                <span>Attendance History</span>
            </a>
        </c:if>

        <div class="sidebar-section-label">Analytics</div>
        <a href="${pageContext.request.contextPath}/reports"
           class="sidebar-link ${activeNav == 'reports' ? 'active' : ''}">
            <i class="bi bi-bar-chart-line"></i>
            <span>Attendance Reports</span>
        </a>
    </nav>

    <div class="sidebar-footer">
        <div class="small text-white-50 mb-2">
            Signed in as <strong class="text-white"><c:out value="${sessionScope.loggedInUser.username}"/></strong>
        </div>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm w-100">
            <i class="bi bi-box-arrow-right me-1"></i>Sign Out
        </a>
    </div>
</aside>

