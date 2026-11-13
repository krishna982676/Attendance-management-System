<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Teacher Dashboard" scope="request"/>
<c:set var="pageSubtitle" value="Manage attendance and view reports for your assigned classes" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="row g-3 mb-4">
    <div class="col-md-4">
        <div class="stat-card">
            <div class="stat-label">Assigned Classes</div>
            <div class="stat-value"><c:out value="${assignedClasses.size()}"/></div>
            <div class="small text-muted mt-2">Department: <c:out value="${teacher != null ? teacher.department : 'N/A'}"/></div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="stat-card">
            <div class="stat-label">Subjects Taught</div>
            <div class="stat-value"><c:out value="${subjects.size()}"/></div>
            <div class="small text-muted mt-2">Employee ID: <c:out value="${teacher != null ? teacher.employeeId : 'N/A'}"/></div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="stat-card">
            <div class="stat-label">Students in Selected Class</div>
            <div class="stat-value"><c:out value="${classStudents.size()}"/></div>
            <div class="mt-2">
                <a href="${pageContext.request.contextPath}/attendance?classId=${selectedClassId}" class="btn btn-sm btn-primary">
                    <i class="bi bi-calendar-check me-1"></i>Mark Attendance Now
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Class Selector & Assigned Subjects -->
<div class="card mb-4">
    <div class="card-body d-flex flex-wrap align-items-center justify-content-between gap-3">
        <form action="${pageContext.request.contextPath}/dashboard" method="get" class="d-flex align-items-center gap-2">
            <label for="classId" class="fw-semibold mb-0">Viewing Class:</label>
            <select name="classId" id="classId" class="form-select" style="width: auto;" onchange="this.form.submit()">
                <c:forEach var="cls" items="${assignedClasses}">
                    <option value="${cls.classId}" ${cls.classId == selectedClassId ? 'selected' : ''}>
                        <c:out value="${cls.displayName}"/>
                    </option>
                </c:forEach>
            </select>
        </form>

        <div class="d-flex gap-2">
            <a href="${pageContext.request.contextPath}/attendance?classId=${selectedClassId}" class="btn btn-primary btn-sm">
                <i class="bi bi-check2-square me-1"></i>Mark Attendance
            </a>
            <a href="${pageContext.request.contextPath}/reports?classId=${selectedClassId}" class="btn btn-outline-primary btn-sm">
                <i class="bi bi-bar-chart me-1"></i>Class Report
            </a>
        </div>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header">
                <i class="bi bi-people me-2 text-primary"></i>Class Attendance Overview
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Roll No</th>
                            <th>Student Name</th>
                            <th>Present / Total</th>
                            <th>%</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty classReport}">
                                <tr><td colspan="4" class="text-center text-muted py-4">No students in selected class.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${classReport}">
                                    <tr class="${row.belowThreshold ? 'row-below-threshold' : ''}">
                                        <td class="fw-semibold"><c:out value="${row.rollNumber}"/></td>
                                        <td><c:out value="${row.studentName}"/></td>
                                        <td><c:out value="${row.presentDays}"/> / <c:out value="${row.totalDays}"/></td>
                                        <td>
                                            <span class="badge ${row.belowThreshold ? 'bg-danger' : 'bg-success'}">
                                                <c:out value="${row.attendancePercentage}"/>%
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

    <div class="col-lg-6">
        <div class="card h-100">
            <div class="card-header">
                <i class="bi bi-clock-history me-2 text-primary"></i>Recent Class Attendance History
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Roll No</th>
                            <th>Student</th>
                            <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty recentHistory}">
                                <tr><td colspan="4" class="text-center text-muted py-4">No attendance history yet.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="att" items="${recentHistory}">
                                    <tr>
                                        <td><c:out value="${att.attendanceDate}"/></td>
                                        <td class="fw-semibold"><c:out value="${att.rollNumber}"/></td>
                                        <td><c:out value="${att.studentName}"/></td>
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
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

