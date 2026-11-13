<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Student Dashboard" scope="request"/>
<c:set var="pageSubtitle" value="Personal attendance summary and history" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<c:choose>
    <c:when test="${missingProfile}">
        <div class="alert alert-warning">
            Your user account is not linked to a student profile yet. Please contact the Administrator.
        </div>
    </c:when>
    <c:otherwise>
        <!-- Student Info Header -->
        <div class="card mb-4">
            <div class="card-body d-flex flex-wrap justify-content-between align-items-center gap-3">
                <div>
                    <h4 class="fw-bold mb-1"><c:out value="${student.name}"/></h4>
                    <div class="text-muted">
                        Roll Number: <strong><c:out value="${student.rollNumber}"/></strong> &bull;
                        Class: <strong><c:out value="${student.className}"/></strong> &bull;
                        Department: <strong><c:out value="${student.department}"/></strong>
                    </div>
                </div>
                <c:if test="${summary != null}">
                    <div>
                        <c:choose>
                            <c:when test="${summary.belowThreshold}">
                                <span class="badge bg-danger fs-6 px-3 py-2">
                                    <i class="bi bi-exclamation-triangle-fill me-1"></i>Below 75% Requirement
                                </span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-success fs-6 px-3 py-2">
                                    <i class="bi bi-check-circle-fill me-1"></i>Good Attendance Standing
                                </span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- Summary Cards -->
        <div class="row g-3 mb-4">
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-label">Total Working Days</div>
                    <div class="stat-value"><c:out value="${summary != null ? summary.totalDays : 0}"/></div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-label">Total Present Days</div>
                    <div class="stat-value text-success"><c:out value="${summary != null ? summary.presentDays : 0}"/></div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-label">Total Absent Days</div>
                    <div class="stat-value text-danger"><c:out value="${summary != null ? summary.absentDays : 0}"/></div>
                </div>
            </div>
            <div class="col-sm-6 col-xl-3">
                <div class="stat-card">
                    <div class="stat-label">Attendance Percentage</div>
                    <div class="stat-value ${summary != null && summary.belowThreshold ? 'text-danger' : 'text-primary'}">
                        <c:out value="${summary != null ? summary.attendancePercentage : 0.0}"/>%
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${summary != null && summary.belowThreshold}">
            <div class="alert alert-danger d-flex align-items-center mb-4">
                <i class="bi bi-exclamation-octagon-fill fs-4 me-3"></i>
                <div>
                    <strong>Attendance Shortage Warning:</strong> Your overall attendance is
                    <strong><c:out value="${summary.attendancePercentage}"/>%</strong>, which is below the minimum 75% requirement.
                </div>
            </div>
        </c:if>

        <!-- Filter & Attendance History -->
        <div class="card">
            <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
                <span><i class="bi bi-calendar3 me-2 text-primary"></i>Attendance History</span>
                <form action="${pageContext.request.contextPath}/dashboard" method="get" class="d-flex flex-wrap align-items-center gap-2">
                    <select name="viewMode" class="form-select form-select-sm" style="width: auto;">
                        <option value="monthly" ${viewMode == 'monthly' ? 'selected' : ''}>Monthly View</option>
                        <option value="all" ${viewMode == 'all' ? 'selected' : ''}>All Records</option>
                    </select>
                    <select name="month" class="form-select form-select-sm" style="width: auto;">
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>Month ${m}</option>
                        </c:forEach>
                    </select>
                    <input type="number" name="year" value="${selectedYear}" class="form-control form-control-sm" style="width: 90px;">
                    <button type="submit" class="btn btn-sm btn-primary">Filter</button>
                </form>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Class</th>
                            <th>Subject</th>
                            <th>Status</th>
                            <th>Remarks</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty history}">
                                <tr><td colspan="5" class="text-center text-muted py-4">No attendance records found for this period.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="att" items="${history}">
                                    <tr>
                                        <td class="fw-semibold"><c:out value="${att.attendanceDate}"/></td>
                                        <td><c:out value="${att.className}"/></td>
                                        <td><c:out value="${att.subjectName != null ? att.subjectName : 'General'}"/></td>
                                        <td>
                                            <span class="badge rounded-pill ${att.status == 'Present' ? 'badge-present' : 'badge-absent'}">
                                                <c:out value="${att.status}"/>
                                            </span>
                                        </td>
                                        <td class="text-muted"><c:out value="${att.remarks != null ? att.remarks : '-'}"/></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

