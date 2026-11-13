<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="isUpdate" value="${formMode == 'update'}"/>
<c:set var="pageTitle" value="${isUpdate ? 'Update Teacher' : 'Add New Teacher'}" scope="request"/>
<c:set var="pageSubtitle" value="Manage faculty details, class assignment, and login access" scope="request"/>
<c:set var="activeNav" value="teachers" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-person-badge-fill me-2 text-primary"></i><c:out value="${pageTitle}"/></span>
                <a href="${pageContext.request.contextPath}/teachers" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Back to Teachers
                </a>
            </div>
            <div class="card-body p-4">
                <c:if test="${not empty error}">
                    <div class="alert alert-danger d-flex align-items-center">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <div><c:out value="${error}"/></div>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/teachers" method="post">
                    <input type="hidden" name="action" value="${isUpdate ? 'update' : 'create'}">
                    <c:if test="${isUpdate}">
                        <input type="hidden" name="teacherId" value="${teacher.teacherId}">
                    </c:if>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="employeeId" class="form-label fw-semibold">Employee ID <span class="text-danger">*</span></label>
                            <input type="text" id="employeeId" name="employeeId" class="form-control"
                                   value="<c:out value='${teacher.employeeId}'/>" placeholder="e.g., EMP-CSE-103" required>
                        </div>

                        <div class="col-md-6">
                            <label for="name" class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                            <input type="text" id="name" name="name" class="form-control"
                                   value="<c:out value='${teacher.name}'/>" placeholder="e.g., Prof. Neha Kulkarni" required>
                        </div>

                        <div class="col-md-6">
                            <label for="email" class="form-label fw-semibold">Email Address <span class="text-danger">*</span></label>
                            <input type="email" id="email" name="email" class="form-control"
                                   value="<c:out value='${teacher.email}'/>" placeholder="teacher@college.edu" required>
                        </div>

                        <div class="col-md-6">
                            <label for="phone" class="form-label fw-semibold">Phone Number <span class="text-danger">*</span></label>
                            <input type="text" id="phone" name="phone" class="form-control"
                                   value="<c:out value='${teacher.phone}'/>" placeholder="e.g., 9876501003" required>
                        </div>

                        <div class="col-md-6">
                            <label for="department" class="form-label fw-semibold">Department <span class="text-danger">*</span></label>
                            <input type="text" id="department" name="department" class="form-control"
                                   value="<c:out value='${teacher.department}'/>" placeholder="e.g., Computer Science & Engineering" required>
                        </div>

                        <div class="col-md-6">
                            <label for="assignedClassId" class="form-label fw-semibold">Assigned Class</label>
                            <select id="assignedClassId" name="assignedClassId" class="form-select">
                                <option value="">-- Optional Class Assignment --</option>
                                <c:forEach var="cls" items="${classes}">
                                    <option value="${cls.classId}" ${cls.classId == teacher.assignedClassId ? 'selected' : ''}>
                                        <c:out value="${cls.displayName}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <c:if test="${!isUpdate}">
                        <hr class="my-4">
                        <h6 class="fw-bold mb-1">Teacher Login Credentials</h6>
                        <p class="text-muted small mb-3">Used by the teacher to sign in and mark class attendance.</p>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="loginUsername" class="form-label fw-semibold">Username <span class="text-danger">*</span></label>
                                <input type="text" id="loginUsername" name="loginUsername" class="form-control"
                                       value="<c:out value='${loginUsername}'/>" placeholder="e.g., teacher3" required>
                            </div>
                            <div class="col-md-6">
                                <label for="loginPassword" class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
                                <input type="password" id="loginPassword" name="loginPassword" class="form-control"
                                       placeholder="Min 6 characters" required>
                            </div>
                        </div>
                    </c:if>

                    <div class="mt-4 d-flex justify-content-end gap-2">
                        <a href="${pageContext.request.contextPath}/teachers" class="btn btn-outline-secondary">Cancel</a>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i>${isUpdate ? 'Update Teacher' : 'Save Teacher'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

