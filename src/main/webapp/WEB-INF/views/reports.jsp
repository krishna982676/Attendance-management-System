<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Attendance Reports" scope="request"/>
<c:set var="pageSubtitle" value="Daily, monthly, and overall attendance percentages with 75% shortage tracking" scope="request"/>
<c:set var="activeNav" value="reports" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<!-- Report Type Tabs & Filters -->
<div class="card mb-4 no-print">
    <div class="card-body">
        <ul class="nav nav-pills mb-3">
            <li class="nav-item">
                <a class="nav-link ${reportType == 'monthly' ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/reports?reportType=monthly">
                    <i class="bi bi-calendar-month me-1"></i>Monthly Report
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${reportType == 'daily' ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/reports?reportType=daily">
                    <i class="bi bi-calendar-day me-1"></i>Daily Report
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${reportType == 'overall' ? 'active' : ''}"
                   href="${pageContext.request.contextPath}/reports?reportType=overall">
                    <i class="bi bi-graph-up me-1"></i>Overall Report
                </a>
            </li>
        </ul>

        <form action="${pageContext.request.contextPath}/reports" method="get" class="row g-3 align-items-end">
            <input type="hidden" name="reportType" value="<c:out value='${reportType}'/>">

            <c:if test="${!sessionScope.loggedInUser.student}">
                <div class="col-md-3">
                    <label class="form-label fw-semibold">Class</label>
                    <select name="classId" class="form-select">
                        <c:if test="${sessionScope.loggedInUser.admin}">
                            <option value="">All Classes</option>
                        </c:if>
                        <c:forEach var="cls" items="${classes}">
                            <option value="${cls.classId}" ${cls.classId == selectedClassId ? 'selected' : ''}>
                                <c:out value="${cls.displayName}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="col-md-3">
                    <label class="form-label fw-semibold">Student</label>
                    <select name="studentId" class="form-select">
                        <option value="">All Students</option>
                        <c:forEach var="stu" items="${students}">
                            <option value="${stu.studentId}" ${stu.studentId == selectedStudentId ? 'selected' : ''}>
                                <c:out value="${stu.rollNumber}"/> - <c:out value="${stu.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${reportType == 'daily'}">
                    <div class="col-md-3">
                        <label class="form-label fw-semibold">Date</label>
                        <input type="date" name="date" value="<c:out value='${selectedDate}'/>" class="form-control">
                    </div>
                </c:when>
                <c:when test="${reportType == 'monthly'}">
                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Month</label>
                        <select name="month" class="form-select">
                            <c:forEach var="m" begin="1" end="12">
                                <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>Month ${m}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label fw-semibold">Year</label>
                        <input type="number" name="year" value="${selectedYear}" class="form-control">
                    </div>
                </c:when>
            </c:choose>

            <c:if test="${reportType != 'daily'}">
                <div class="col-md-2">
                    <div class="form-check mb-2">
                        <input class="form-check-input" type="checkbox" name="onlyLow" value="true" id="onlyLowCheck"
                               ${onlyLow ? 'checked' : ''}>
                        <label class="form-check-label fw-semibold text-danger" for="onlyLowCheck">
                            Below 75% Only
                        </label>
                    </div>
                </div>
            </c:if>

            <div class="col-md-auto d-flex gap-2">
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-funnel me-1"></i>Generate
                </button>
                <button type="button" class="btn btn-outline-dark" onclick="window.print()">
                    <i class="bi bi-printer me-1"></i>Print
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Report Results -->
<c:choose>
    <c:when test="${reportType == 'daily'}">
        <div class="row g-3 mb-4">
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-label">Report Date</div>
                    <div class="stat-value fs-4"><c:out value="${selectedDate}"/></div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-label">Total Present</div>
                    <div class="stat-value text-success"><c:out value="${presentCount}"/></div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="stat-card">
                    <div class="stat-label">Total Absent</div>
                    <div class="stat-value text-danger"><c:out value="${absentCount}"/></div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <i class="bi bi-calendar-day me-2 text-primary"></i>Daily Attendance Report (<c:out value="${selectedDate}"/>)
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Roll No</th>
                            <th>Student Name</th>
                            <th>Class</th>
                            <th>Subject</th>
                            <th>Status</th>
                            <th>Remarks</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty dailyRecords}">
                                <tr><td colspan="6" class="text-center text-muted py-4">No attendance recorded for this date.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="rec" items="${dailyRecords}">
                                    <tr>
                                        <td class="fw-bold text-primary"><c:out value="${rec.rollNumber}"/></td>
                                        <td class="fw-semibold"><c:out value="${rec.studentName}"/></td>
                                        <td><c:out value="${rec.className}"/></td>
                                        <td><c:out value="${rec.subjectName != null ? rec.subjectName : 'General'}"/></td>
                                        <td>
                                            <span class="badge rounded-pill ${rec.status == 'Present' ? 'badge-present' : 'badge-absent'}">
                                                <c:out value="${rec.status}"/>
                                            </span>
                                        </td>
                                        <td class="text-muted"><c:out value="${rec.remarks != null ? rec.remarks : '-'}"/></td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </c:when>

    <c:otherwise>
        <c:if test="${lowAttendanceCount > 0}">
            <div class="alert alert-danger d-flex align-items-center mb-4">
                <i class="bi bi-exclamation-triangle-fill fs-5 me-3"></i>
                <div>
                    <strong><c:out value="${lowAttendanceCount}"/></strong> student(s) have attendance below the 75% threshold
                    and are highlighted in red below.
                </div>
            </div>
        </c:if>

        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span>
                    <i class="bi bi-bar-chart-line me-2 text-primary"></i>
                    ${reportType == 'overall' ? 'Overall' : 'Monthly'} Attendance Percentage Report
                </span>
                <span class="text-muted small">Formula: (Present Days / Total Working Days) &times; 100</span>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Roll No</th>
                            <th>Student Name</th>
                            <th>Class</th>
                            <th>Department</th>
                            <th class="text-center">Present</th>
                            <th class="text-center">Absent</th>
                            <th class="text-center">Total Days</th>
                            <th>Attendance %</th>
                            <th>Standing</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty reportRows}">
                                <tr><td colspan="9" class="text-center text-muted py-4">No records found for this filter.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="row" items="${reportRows}">
                                    <tr class="${row.belowThreshold ? 'row-below-threshold' : ''}">
                                        <td class="fw-bold text-primary"><c:out value="${row.rollNumber}"/></td>
                                        <td class="fw-semibold"><c:out value="${row.studentName}"/></td>
                                        <td><c:out value="${row.className}"/></td>
                                        <td class="small text-muted"><c:out value="${row.department}"/></td>
                                        <td class="text-center fw-semibold text-success"><c:out value="${row.presentDays}"/></td>
                                        <td class="text-center fw-semibold text-danger"><c:out value="${row.absentDays}"/></td>
                                        <td class="text-center fw-semibold"><c:out value="${row.totalDays}"/></td>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="progress flex-grow-1" style="height: 8px; min-width: 70px;">
                                                    <div class="progress-bar ${row.belowThreshold ? 'bg-danger' : 'bg-success'}"
                                                         style="width: ${row.attendancePercentage}%"></div>
                                                </div>
                                                <span class="fw-bold ${row.belowThreshold ? 'text-danger' : 'text-success'}">
                                                    <c:out value="${row.attendancePercentage}"/>%
                                                </span>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${row.totalDays == 0}">
                                                    <span class="badge bg-secondary">No Data</span>
                                                </c:when>
                                                <c:when test="${row.belowThreshold}">
                                                    <span class="badge bg-danger">
                                                        <i class="bi bi-exclamation-circle me-1"></i>Below 75%
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-success">
                                                        <i class="bi bi-check-circle me-1"></i>Eligible
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
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
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

