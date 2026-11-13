<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Attendance History" scope="request"/>
<c:set var="pageSubtitle" value="Review past attendance logs and update individual records" scope="request"/>
<c:set var="activeNav" value="attendance-history" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="card mb-4">
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/attendance" method="get" class="row g-3 align-items-end">
            <input type="hidden" name="action" value="history">
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
            <div class="col-md-2">
                <label class="form-label fw-semibold">From Date</label>
                <input type="date" name="fromDate" value="<c:out value='${fromDate}'/>" class="form-control">
            </div>
            <div class="col-md-2">
                <label class="form-label fw-semibold">To Date</label>
                <input type="date" name="toDate" value="<c:out value='${toDate}'/>" class="form-control">
            </div>
            <div class="col-md-2 d-flex gap-2">
                <button type="submit" class="btn btn-primary w-100">Filter</button>
                <a href="${pageContext.request.contextPath}/attendance?action=history" class="btn btn-outline-secondary">Reset</a>
            </div>
        </form>
    </div>
</div>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <span><i class="bi bi-clock-history me-2 text-primary"></i>Attendance Log (<c:out value="${history.size()}"/> records)</span>
        <a href="${pageContext.request.contextPath}/attendance" class="btn btn-sm btn-primary">
            <i class="bi bi-calendar-check me-1"></i>Mark Attendance
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
                    <th>Subject</th>
                    <th>Status</th>
                    <th>Remarks</th>
                    <th>Marked By</th>
                    <th class="text-end">Quick Update</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty history}">
                        <tr><td colspan="9" class="text-center text-muted py-4">No attendance records match the selected filters.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="att" items="${history}">
                            <tr>
                                <td class="fw-semibold"><c:out value="${att.attendanceDate}"/></td>
                                <td class="fw-bold text-primary"><c:out value="${att.rollNumber}"/></td>
                                <td><c:out value="${att.studentName}"/></td>
                                <td><c:out value="${att.className}"/></td>
                                <td><c:out value="${att.subjectName != null ? att.subjectName : 'General'}"/></td>
                                <td>
                                    <span class="badge rounded-pill ${att.status == 'Present' ? 'badge-present' : 'badge-absent'}">
                                        <c:out value="${att.status}"/>
                                    </span>
                                </td>
                                <td class="text-muted small"><c:out value="${att.remarks != null ? att.remarks : '-'}"/></td>
                                <td class="small"><c:out value="${att.markedByName != null ? att.markedByName : 'System'}"/></td>
                                <td class="text-end">
                                    <form action="${pageContext.request.contextPath}/attendance" method="post" class="d-inline-flex gap-1 align-items-center">
                                        <input type="hidden" name="action" value="updateSingle">
                                        <input type="hidden" name="attendanceId" value="${att.attendanceId}">
                                        <input type="hidden" name="remarks" value="<c:out value='${att.remarks}'/>">
                                        <select name="status" class="form-select form-select-sm" style="width: 105px;">
                                            <option value="Present" ${att.status == 'Present' ? 'selected' : ''}>Present</option>
                                            <option value="Absent" ${att.status == 'Absent' ? 'selected' : ''}>Absent</option>
                                        </select>
                                        <button type="submit" class="btn btn-sm btn-outline-primary" title="Save Status">
                                            <i class="bi bi-check2"></i>
                                        </button>
                                    </form>
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

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

