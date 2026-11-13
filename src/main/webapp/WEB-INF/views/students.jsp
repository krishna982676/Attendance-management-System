<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Student Management" scope="request"/>
<c:set var="pageSubtitle" value="View, search, add, update, and assign students to classes" scope="request"/>
<c:set var="activeNav" value="students" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="card mb-4">
    <div class="card-body">
        <div class="row g-3 align-items-center">
            <div class="col-lg-8">
                <form action="${pageContext.request.contextPath}/students" method="get" class="row g-2">
                    <div class="col-md-5">
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                            <input type="text" name="q" value="<c:out value='${searchQuery}'/>"
                                   class="form-control" placeholder="Search by name, roll no, or dept...">
                        </div>
                    </div>
                    <div class="col-md-4">
                        <select name="classId" class="form-select">
                            <option value="">All Classes</option>
                            <c:forEach var="cls" items="${classes}">
                                <option value="${cls.classId}" ${cls.classId == selectedClassId ? 'selected' : ''}>
                                    <c:out value="${cls.displayName}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-3 d-flex gap-2">
                        <button type="submit" class="btn btn-primary w-100">Filter</button>
                        <a href="${pageContext.request.contextPath}/students" class="btn btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>

            <c:if test="${sessionScope.loggedInUser.admin}">
                <div class="col-lg-4 text-lg-end">
                    <a href="${pageContext.request.contextPath}/students?action=new" class="btn btn-success">
                        <i class="bi bi-person-plus-fill me-1"></i>Add New Student
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-header d-flex justify-content-between align-items-center">
        <span><i class="bi bi-mortarboard me-2 text-primary"></i>Registered Students (<c:out value="${students.size()}"/>)</span>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Roll Number</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Class</th>
                    <th>Department</th>
                    <c:if test="${sessionScope.loggedInUser.admin}">
                        <th class="text-end">Actions</th>
                    </c:if>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty students}">
                        <tr>
                            <td colspan="8" class="text-center text-muted py-4">No matching students found.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="stu" items="${students}" varStatus="status">
                            <tr>
                                <td>#${status.count}</td>
                                <td class="fw-bold text-primary"><c:out value="${stu.rollNumber}"/></td>
                                <td class="fw-semibold">
                                    <c:out value="${stu.name}"/>
                                    <c:if test="${not empty stu.username}">
                                        <span class="badge bg-light text-secondary border ms-1" title="Portal Login">
                                            @<c:out value="${stu.username}"/>
                                        </span>
                                    </c:if>
                                </td>
                                <td><c:out value="${stu.email}"/></td>
                                <td><c:out value="${stu.phone}"/></td>
                                <td>
                                    <span class="badge bg-primary bg-opacity-10 text-primary">
                                        <c:out value="${stu.className != null ? stu.className : 'Unassigned'}"/>
                                    </span>
                                </td>
                                <td><c:out value="${stu.department}"/></td>
                                <c:if test="${sessionScope.loggedInUser.admin}">
                                    <td class="text-end">
                                        <a href="${pageContext.request.contextPath}/students?action=edit&id=${stu.studentId}"
                                           class="btn btn-sm btn-outline-primary me-1" title="Edit">
                                            <i class="bi bi-pencil-square"></i>
                                        </a>
                                        <form id="delStudentForm_${stu.studentId}"
                                              action="${pageContext.request.contextPath}/students" method="post" class="d-inline">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${stu.studentId}">
                                            <button type="button" class="btn btn-sm btn-outline-danger"
                                                    onclick="confirmDelete('delStudentForm_${stu.studentId}', '${stu.name}')" title="Delete">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </form>
                                    </td>
                                </c:if>
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

