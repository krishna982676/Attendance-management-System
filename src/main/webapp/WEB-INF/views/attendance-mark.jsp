<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Mark Attendance" scope="request"/>
<c:set var="pageSubtitle" value="Select class, subject, and date to record or update attendance" scope="request"/>
<c:set var="activeNav" value="attendance-mark" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<!-- Class, Subject & Date Selector -->
<div class="card mb-4">
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/attendance" method="get" class="row g-3 align-items-end">
            <div class="col-md-4">
                <label for="classId" class="form-label fw-semibold">Select Class</label>
                <select name="classId" id="classId" class="form-select" onchange="this.form.submit()" required>
                    <c:forEach var="cls" items="${classes}">
                        <option value="${cls.classId}" ${cls.classId == selectedClassId ? 'selected' : ''}>
                            <c:out value="${cls.displayName}"/> (<c:out value="${cls.studentCount}"/> students)
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="subjectId" class="form-label fw-semibold">Select Subject</label>
                <select name="subjectId" id="subjectId" class="form-select" onchange="this.form.submit()">
                    <option value="">General / Class Attendance</option>
                    <c:forEach var="sub" items="${subjects}">
                        <option value="${sub.subjectId}" ${sub.subjectId == selectedSubjectId ? 'selected' : ''}>
                            <c:out value="${sub.subjectCode}"/> - <c:out value="${sub.subjectName}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-3">
                <label for="date" class="form-label fw-semibold">Attendance Date</label>
                <input type="date" name="date" id="date" class="form-control" value="<c:out value='${selectedDate}'/>" required>
            </div>
            <div class="col-md-2">
                <button type="submit" class="btn btn-primary w-100">
                    <i class="bi bi-people-fill me-1"></i>Load Roster
                </button>
            </div>
        </form>
    </div>
</div>

<c:if test="${alreadyMarked}">
    <div class="alert alert-warning d-flex align-items-center mb-4">
        <i class="bi bi-info-circle-fill fs-5 me-3"></i>
        <div>
            <strong>Attendance Already Recorded:</strong> Attendance for this selection on
            <strong><c:out value="${selectedDate}"/></strong> already exists in MongoDB.
            Duplicate inserts are blocked; submitting this form will <strong>update</strong> the existing records.
        </div>
    </div>
</c:if>

<c:choose>
    <c:when test="${empty classes}">
        <div class="alert alert-info">No classes are currently available or assigned to your account.</div>
    </c:when>
    <c:when test="${empty students}">
        <div class="alert alert-info">No students are registered in the selected class yet.</div>
    </c:when>
    <c:otherwise>
        <form action="${pageContext.request.contextPath}/attendance" method="post">
            <input type="hidden" name="action" value="save">
            <input type="hidden" name="classId" value="${selectedClassId}">
            <input type="hidden" name="subjectId" value="${selectedSubjectId}">
            <input type="hidden" name="attendanceDate" value="<c:out value='${selectedDate}'/>">
            <input type="hidden" name="mode" value="${alreadyMarked ? 'update' : 'create'}">

            <div class="card">
                <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div class="d-flex flex-wrap align-items-center gap-3">
                        <span class="fw-bold"><i class="bi bi-list-check me-2 text-primary"></i>Student Roster</span>
                        <span class="badge badge-present rounded-pill">
                            Present: <span id="livePresentCount">0</span>
                        </span>
                        <span class="badge badge-absent rounded-pill">
                            Absent: <span id="liveAbsentCount">0</span>
                        </span>
                    </div>

                    <div class="d-flex flex-wrap align-items-center gap-2">
                        <button type="button" class="btn btn-sm btn-outline-success" onclick="markAllAttendance('Present')">
                            <i class="bi bi-check-all me-1"></i>All Present
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-danger" onclick="markAllAttendance('Absent')">
                            <i class="bi bi-x-circle me-1"></i>All Absent
                        </button>
                    </div>
                </div>

                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                            <tr>
                                <th>#</th>
                                <th>Roll Number</th>
                                <th>Student Name</th>
                                <th>Department</th>
                                <th>Status</th>
                                <th>Remarks (Optional)</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="stu" items="${students}" varStatus="loop">
                                <c:set var="existingAtt" value="${existingMap[stu.studentId]}"/>
                                <c:set var="currentStatus" value="${existingAtt != null ? existingAtt.status : 'Present'}"/>
                                <tr>
                                    <td>${loop.index + 1}</td>
                                    <td class="fw-bold text-primary">
                                        <c:out value="${stu.rollNumber}"/>
                                        <input type="hidden" name="studentId" value="${stu.studentId}">
                                    </td>
                                    <td class="fw-semibold"><c:out value="${stu.name}"/></td>
                                    <td class="text-muted small"><c:out value="${stu.department}"/></td>
                                    <td>
                                        <div class="btn-group status-toggle-group" role="group">
                                            <input type="radio" class="btn-check attendance-status-radio"
                                                   name="status_${stu.studentId}" id="present_${stu.studentId}"
                                                   value="Present" ${currentStatus == 'Present' ? 'checked' : ''}>
                                            <label class="btn btn-outline-success btn-sm" for="present_${stu.studentId}">
                                                <i class="bi bi-check-lg me-1"></i>Present
                                            </label>

                                            <input type="radio" class="btn-check attendance-status-radio"
                                                   name="status_${stu.studentId}" id="absent_${stu.studentId}"
                                                   value="Absent" ${currentStatus == 'Absent' ? 'checked' : ''}>
                                            <label class="btn btn-outline-danger btn-sm" for="absent_${stu.studentId}">
                                                <i class="bi bi-x-lg me-1"></i>Absent
                                            </label>
                                        </div>
                                    </td>
                                    <td>
                                        <input type="text" name="remarks_${stu.studentId}" class="form-control form-control-sm"
                                               value="<c:out value='${existingAtt != null ? existingAtt.remarks : ""}'/>"
                                               placeholder="Optional note...">
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="card-footer bg-white d-flex justify-content-end py-3">
                    <button type="submit" class="btn ${alreadyMarked ? 'btn-warning' : 'btn-primary'} btn-lg">
                        <i class="bi bi-save me-2"></i>
                        ${alreadyMarked ? 'Update Attendance Records' : 'Save Attendance'}
                    </button>
                </div>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
