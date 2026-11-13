<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Classes & Subjects" scope="request"/>
<c:set var="pageSubtitle" value="Manage academic classes, sections, and assigned subjects" scope="request"/>
<c:set var="activeNav" value="classes" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="row g-4">
    <!-- Classes Column -->
    <div class="col-lg-6">
        <div class="card mb-4">
            <div class="card-header">
                <i class="bi bi-plus-circle me-2 text-primary"></i>
                <c:out value="${editClass != null ? 'Update Class' : 'Add New Class'}"/>
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/classes" method="post" class="row g-3">
                    <input type="hidden" name="action" value="${editClass != null ? 'updateClass' : 'createClass'}">
                    <c:if test="${editClass != null}">
                        <input type="hidden" name="classId" value="${editClass.classId}">
                    </c:if>

                    <div class="col-md-8">
                        <label class="form-label fw-semibold">Class Name *</label>
                        <input type="text" name="className" class="form-control"
                               value="<c:out value='${editClass != null ? editClass.className : ""}'/>"
                               placeholder="e.g., B.Tech CSE Sem 5" required>
                    </div>
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Section *</label>
                        <input type="text" name="section" class="form-control"
                               value="<c:out value='${editClass != null ? editClass.section : ""}'/>"
                               placeholder="e.g., A" required>
                    </div>
                    <div class="col-md-7">
                        <label class="form-label fw-semibold">Department *</label>
                        <input type="text" name="department" class="form-control"
                               value="<c:out value='${editClass != null ? editClass.department : ""}'/>"
                               placeholder="e.g., Computer Science & Engineering" required>
                    </div>
                    <div class="col-md-5">
                        <label class="form-label fw-semibold">Academic Year</label>
                        <input type="text" name="academicYear" class="form-control"
                               value="<c:out value='${editClass != null ? editClass.academicYear : "2026-2027"}'/>"
                               placeholder="2026-2027">
                    </div>
                    <div class="col-12 d-flex justify-content-end gap-2">
                        <c:if test="${editClass != null}">
                            <a href="${pageContext.request.contextPath}/classes" class="btn btn-outline-secondary btn-sm">Cancel</a>
                        </c:if>
                        <button type="submit" class="btn btn-primary btn-sm">
                            <i class="bi bi-save me-1"></i>${editClass != null ? 'Update Class' : 'Add Class'}
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <i class="bi bi-building me-2 text-primary"></i>Registered Classes (<c:out value="${classes.size()}"/>)
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Class</th>
                            <th>Department</th>
                            <th>Students</th>
                            <th class="text-end">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="cls" items="${classes}">
                            <tr>
                                <td>
                                    <div class="fw-semibold"><c:out value="${cls.displayName}"/></div>
                                    <small class="text-muted"><c:out value="${cls.academicYear}"/></small>
                                </td>
                                <td><c:out value="${cls.department}"/></td>
                                <td><span class="badge bg-primary bg-opacity-10 text-primary"><c:out value="${cls.studentCount}"/></span></td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/classes?editClassId=${cls.classId}"
                                       class="btn btn-sm btn-outline-primary me-1"><i class="bi bi-pencil"></i></a>
                                    <form id="delClass_${cls.classId}" action="${pageContext.request.contextPath}/classes" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="deleteClass">
                                        <input type="hidden" name="classId" value="${cls.classId}">
                                        <button type="button" class="btn btn-sm btn-outline-danger"
                                                onclick="confirmDelete('delClass_${cls.classId}', '${cls.displayName}')">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- Subjects Column -->
    <div class="col-lg-6">
        <div class="card mb-4">
            <div class="card-header">
                <i class="bi bi-book me-2 text-primary"></i>Add New Subject
            </div>
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/classes" method="post" class="row g-3">
                    <input type="hidden" name="action" value="createSubject">
                    <div class="col-md-4">
                        <label class="form-label fw-semibold">Code *</label>
                        <input type="text" name="subjectCode" class="form-control" placeholder="e.g., CS504" required>
                    </div>
                    <div class="col-md-8">
                        <label class="form-label fw-semibold">Subject Name *</label>
                        <input type="text" name="subjectName" class="form-control" placeholder="e.g., Computer Networks" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Class *</label>
                        <select name="classId" class="form-select" required>
                            <option value="">-- Select Class --</option>
                            <c:forEach var="cls" items="${classes}">
                                <option value="${cls.classId}"><c:out value="${cls.displayName}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Assigned Teacher</label>
                        <select name="teacherId" class="form-select">
                            <option value="">-- Optional --</option>
                            <c:forEach var="t" items="${teachers}">
                                <option value="${t.teacherId}"><c:out value="${t.name}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-12 text-end">
                        <button type="submit" class="btn btn-success btn-sm">
                            <i class="bi bi-plus-lg me-1"></i>Add Subject
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <div class="card">
            <div class="card-header">
                <i class="bi bi-journals me-2 text-primary"></i>Subjects (<c:out value="${subjects.size()}"/>)
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover mb-0">
                        <thead>
                        <tr>
                            <th>Code</th>
                            <th>Subject</th>
                            <th>Class</th>
                            <th>Teacher</th>
                            <th class="text-end">Action</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="sub" items="${subjects}">
                            <tr>
                                <td class="fw-bold text-primary"><c:out value="${sub.subjectCode}"/></td>
                                <td><c:out value="${sub.subjectName}"/></td>
                                <td><c:out value="${sub.classDisplayName}"/></td>
                                <td><c:out value="${sub.teacherName != null ? sub.teacherName : 'Unassigned'}"/></td>
                                <td class="text-end">
                                    <form id="delSub_${sub.subjectId}" action="${pageContext.request.contextPath}/classes" method="post" class="d-inline">
                                        <input type="hidden" name="action" value="deleteSubject">
                                        <input type="hidden" name="subjectId" value="${sub.subjectId}">
                                        <button type="button" class="btn btn-sm btn-outline-danger"
                                                onclick="confirmDelete('delSub_${sub.subjectId}', '${sub.subjectName}')">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

