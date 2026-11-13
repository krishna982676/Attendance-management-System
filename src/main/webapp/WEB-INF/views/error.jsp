<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${errorTitle != null ? errorTitle : 'Something Went Wrong'}" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="row justify-content-center py-5">
    <div class="col-md-7">
        <div class="card text-center p-4">
            <div class="card-body">
                <i class="bi bi-exclamation-octagon text-danger" style="font-size: 3rem;"></i>
                <h3 class="fw-bold mt-3"><c:out value="${errorTitle != null ? errorTitle : 'Application Error'}"/></h3>
                <p class="text-muted mb-4">
                    <c:out value="${errorMessage != null ? errorMessage : 'An unexpected error occurred while processing your request.'}"/>
                </p>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">
                    <i class="bi bi-house-door me-1"></i>Return to Dashboard
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

