<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="isUpdate" value="${formMode == 'update'}"/>
<c:set var="pageTitle" value="${isUpdate ? 'Update Student' : 'Add New Student'}" scope="request"/>
<c:set var="pageSubtitle" value="Enter student profile and class assignment details" scope="request"/>
<c:set var="activeNav" value="students" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <span><i class="bi bi-person-lines-fill me-2 text-primary"></i><c:out value="${pageTitle}"/></span>
                <a href="${pageContext.request.contextPath}/students" class="btn btn-sm btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Back to Students
                </a>
            </div>
            <div class="card-body p-4">
                <c:if test="${not empty error}">
                    <div class="alert alert-danger d-flex align-items-center">
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <div><c:out value="${error}"/></div>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/students" method="post">
                    <input type="hidden" name="action" value="${isUpdate ? 'update' : 'create'}">
                    <c:if test="${isUpdate}">
                        <input type="hidden" name="studentId" value="${student.studentId}">
                    </c:if>

                    <div class="row g-3">
                        <div class="col-md-6">
                            <label for="name" class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                            <input type="text" id="name" name="name" class="form-control"
                                   value="<c:out value='${student.name}'/>" placeholder="e.g., Rohan Gupta" required>
                        </div>

                        <div class="col-md-6">
                            <label for="rollNumber" class="form-label fw-semibold">Roll Number <span class="text-danger">*</span></label>
                            <input type="text" id="rollNumber" name="rollNumber" class="form-control"
                                   value="<c:out value='${student.rollNumber}'/>" placeholder="e.g., CS507" required>
                        </div>

                        <div class="col-md-6">
                            <label for="email" class="form-label fw-semibold">Email Address <span class="text-danger">*</span></label>
                            <input type="email" id="email" name="email" class="form-control"
                                   value="<c:out value='${student.email}'/>" placeholder="student@college.edu" required>
                        </div>

                        <div class="col-md-6">
                            <label for="phone" class="form-label fw-semibold">Phone Number <span class="text-danger">*</span></label>
                            <input type="text" id="phone" name="phone" class="form-control"
                                   value="<c:out value='${student.phone}'/>" placeholder="e.g., 9811100007" required>
                        </div>

                        <div class="col-md-6">
                            <label for="classId" class="form-label fw-semibold">Assign Class <span class="text-danger">*</span></label>
                            <select id="classId" name="classId" class="form-select" required>
                                <option value="">-- Select Class --</option>
                                <c:forEach var="cls" items="${classes}">
                                    <option value="${cls.classId}" ${cls.classId == student.classId ? 'selected' : ''}>
                                        <c:out value="${cls.displayName}"/> (<c:out value="${cls.department}"/>)
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-6">
                            <label for="department" class="form-label fw-semibold">Department <span class="text-danger">*</span></label>
                            <input type="text" id="department" name="department" class="form-control"
                                   value="<c:out value='${student.department}'/>"
                                   placeholder="e.g., Computer Science & Engineering" required>
                        </div>
                    </div>

                    <c:if test="${!isUpdate}">
                        <hr class="my-4">
                        <h6 class="fw-bold mb-1">Optional Student Portal Login</h6>
                        <p class="text-muted small mb-3">Provide a username and password if this student should log in to the Student Dashboard.</p>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="loginUsername" class="form-label fw-semibold">Portal Username</label>
                                <input type="text" id="loginUsername" name="loginUsername" class="form-control"
                                       value="<c:out value='${loginUsername}'/>" placeholder="e.g., student7">
                            </div>
                            <div class="col-md-6">
                                <label for="loginPassword" class="form-label fw-semibold">Portal Password</label>
                                <input type="password" id="loginPassword" name="loginPassword" class="form-control"
                                       placeholder="Min 6 characters">
                            </div>
                        </div>
                    </c:if>

                    <div class="mt-4 d-flex justify-content-end gap-2">
                        <a href="${pageContext.request.contextPath}/students" class="btn btn-outline-secondary">Cancel</a>
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save me-1"></i>${isUpdate ? 'Update Student' : 'Save Student'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

