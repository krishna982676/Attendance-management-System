<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Teacher Management" scope="request"/>
<c:set var="pageSubtitle" value="Manage faculty records, login accounts, and class assignments" scope="request"/>
<c:set var="activeNav" value="teachers" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="card mb-4">
    <div class="card-body">
        <div class="row g-3 align-items-center">
            <div class="col-md-8">
                <form action="${pageContext.request.contextPath}/teachers" method="get" class="d-flex gap-2">
                    <div class="input-group">
                        <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                        <input type="text" name="q" value="<c:out value='${searchQuery}'/>"
                               class="form-control" placeholder="Search by teacher name, employee ID, or department...">
                    </div>
                    <button type="submit" class="btn btn-primary">Search</button>
                    <a href="${pageContext.request.contextPath}/teachers" class="btn btn-outline-secondary">Reset</a>
                </form>
            </div>
            <div class="col-md-4 text-md-end">
                <a href="${pageContext.request.contextPath}/teachers?action=new" class="btn btn-success">
                    <i class="bi bi-person-plus-fill me-1"></i>Add New Teacher
                </a>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-header">
        <i class="bi bi-person-badge me-2 text-primary"></i>Faculty Directory (<c:out value="${teachers.size()}"/>)
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover mb-0">
                <thead>
                <tr>
                    <th>Employee ID</th>
                    <th>Name</th>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Department</th>
                    <th>Assigned Class</th>
                    <th class="text-end">Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty teachers}">
                        <tr><td colspan="8" class="text-center text-muted py-4">No teachers found.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="t" items="${teachers}">
                            <tr>
                                <td class="fw-bold text-primary"><c:out value="${t.employeeId}"/></td>
                                <td class="fw-semibold"><c:out value="${t.name}"/></td>
                                <td><span class="badge bg-light text-dark border">@<c:out value="${t.username != null ? t.username : 'N/A'}"/></span></td>
                                <td><c:out value="${t.email}"/></td>
                                <td><c:out value="${t.phone}"/></td>
                                <td><c:out value="${t.department}"/></td>
                                <td>
                                    <span class="badge bg-info bg-opacity-10 text-dark">
                                        <c:out value="${t.assignedClassName != null ? t.assignedClassName : 'Unassigned'}"/>
                                    </span>
                                </td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/teachers?action=edit&id=${t.teacherId}"
                                       class="btn btn-sm btn-outline-primary me-1" title="Edit">
                                        <i class="bi bi-pencil-square"></i>
                                    </a>
                                    <form id="delTeacherForm_${t.teacherId}"
                                          action="${pageContext.request.contextPath}/teachers" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="id" value="${t.teacherId}">
                                        <button type="button" class="btn btn-sm btn-outline-danger"
                                                onclick="confirmDelete('delTeacherForm_${t.teacherId}', '${t.name}')" title="Delete">
                                            <i class="bi bi-trash"></i>
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

