<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Admin Dashboard" scope="request"/>
<c:set var="pageSubtitle" value="Overview of institutional attendance and records" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<!-- KPI Stat Cards -->
<div class="row g-3 mb-4">
    <div class="col-sm-6 col-xl-3">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">Total Students</div>
                    <div class="stat-value"><c:out value="${totalStudents}"/></div>
                </div>
                <div class="stat-icon bg-primary bg-opacity-10 text-primary">
                    <i class="bi bi-mortarboard-fill"></i>
                </div>
            </div>
            <div class="mt-3 small">
                <a href="${pageContext.request.contextPath}/students" class="text-decoration-none fw-medium">
                    Manage students <i class="bi bi-arrow-right"></i>
                </a>
            </div>
        </div>
    </div>

    <div class="col-sm-6 col-xl-3">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">Total Teachers</div>
                    <div class="stat-value"><c:out value="${totalTeachers}"/></div>
                </div>
                <div class="stat-icon bg-info bg-opacity-10 text-info">
                    <i class="bi bi-person-badge-fill"></i>
                </div>
            </div>
            <div class="mt-3 small">
                <a href="${pageContext.request.contextPath}/teachers" class="text-decoration-none fw-medium">
                    Manage teachers <i class="bi bi-arrow-right"></i>
                </a>
            </div>
        </div>
    </div>

    <div class="col-sm-6 col-xl-3">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">Total Classes</div>
                    <div class="stat-value"><c:out value="${totalClasses}"/></div>
                </div>
                <div class="stat-icon bg-warning bg-opacity-10 text-warning">
                    <i class="bi bi-building-fill"></i>
                </div>
            </div>
            <div class="mt-3 small">
                <a href="${pageContext.request.contextPath}/classes" class="text-decoration-none fw-medium">
                    Classes &amp; subjects <i class="bi bi-arrow-right"></i>
                </a>
            </div>
        </div>
    </div>

    <div class="col-sm-6 col-xl-3">
        <div class="stat-card">
            <div class="d-flex justify-content-between align-items-start">
                <div>
                    <div class="stat-label">Today's Attendance (<c:out value="${todayDate}"/>)</div>
                    <div class="stat-value"><c:out value="${todayRate}"/>%</div>
                </div>
                <div class="stat-icon bg-success bg-opacity-10 text-success">
                    <i class="bi bi-check2-circle"></i>
                </div>
            </div>
            <div class="mt-3 small text-muted">
                <span class="text-success fw-semibold"><c:out value="${todayPresent}"/> Present</span> &bull;
                <span class="text-danger fw-semibold"><c:out value="${todayAbsent}"/> Absent</span>
            </div>
        </div>
    </div>
</div>

<!-- Quick Actions & Low Attendance Alert -->
<div class="row g-3 mb-4">
    <div class="col-lg-8">
        <div class="card h-100">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-clock-history me-2 text-primary"></i>Recent Attendance Activity</span>
                <a href="${pageContext.request.contextPath}/attendance?action=history" class="btn btn-sm btn-outline-primary">
                    View All History
                </a>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Roll No</th>
                            <th>Student</th>
                            <th>Class</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty recentAttendance}">
                                <tr>
                                    <td colspan="5" class="text-center text-muted py-4">No attendance records found yet.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="att" items="${recentAttendance}">
                                    <tr>
                                        <td><c:out value="${att.attendanceDate}"/></td>
                                        <td class="fw-semibold"><c:out value="${att.rollNumber}"/></td>
                                        <td><c:out value="${att.studentName}"/></td>
                                        <td><c:out value="${att.className}"/></td>
                                        <td>
                                            <span class="badge rounded-pill ${att.status == 'Present' ? 'badge-present' : 'badge-absent'}">
                                                <c:out value="${att.status}"/>
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-4">
        <div class="card h-100">
            <div class="card-header">
                <i class="bi bi-lightning-charge me-2 text-primary"></i>Quick Actions &amp; Alerts
            </div>
            <div class="card-body d-flex flex-column justify-content-between gap-3">
                <div class="alert ${belowThresholdCount > 0 ? 'alert-danger' : 'alert-success'} mb-0">
                    <div class="fw-bold mb-1">
                        <i class="bi ${belowThresholdCount > 0 ? 'bi-exclamation-triangle-fill' : 'bi-shield-check'} me-1"></i>
                        75% Threshold Monitor
                    </div>
                    <div class="small">
                        <strong><c:out value="${belowThresholdCount}"/></strong> student(s) currently have attendance below 75%.
                    </div>
                    <a href="${pageContext.request.contextPath}/reports?reportType=overall&onlyLow=true"
                       class="btn btn-sm ${belowThresholdCount > 0 ? 'btn-danger' : 'btn-success'} mt-2">
                        View Defaulters Report
                    </a>
                </div>

                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/attendance" class="btn btn-primary">
                        <i class="bi bi-calendar-check me-2"></i>Mark Daily Attendance
                    </a>
                    <a href="${pageContext.request.contextPath}/students?action=new" class="btn btn-outline-primary">
                        <i class="bi bi-person-plus me-2"></i>Register New Student
                    </a>
                    <a href="${pageContext.request.contextPath}/teachers?action=new" class="btn btn-outline-secondary">
                        <i class="bi bi-person-plus-fill me-2"></i>Add New Teacher
                    </a>
                    <a href="${pageContext.request.contextPath}/reports" class="btn btn-outline-dark">
                        <i class="bi bi-file-earmark-bar-graph me-2"></i>Generate Attendance Reports
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

