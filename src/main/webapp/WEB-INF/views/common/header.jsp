<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'Attendance Management System'}"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<div class="sidebar-backdrop" id="sidebarBackdrop"></div>
<div class="app-wrapper">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>

    <div class="main-content">
        <header class="topbar no-print">
            <div class="d-flex align-items-center gap-3">
                <button class="btn btn-outline-secondary d-lg-none" id="sidebarToggleBtn" type="button" aria-label="Toggle menu">
                    <i class="bi bi-list fs-5"></i>
                </button>
                <div>
                    <h5 class="mb-0 fw-bold"><c:out value="${pageTitle != null ? pageTitle : 'Dashboard'}"/></h5>
                    <c:if test="${not empty pageSubtitle}">
                        <div class="text-muted small"><c:out value="${pageSubtitle}"/></div>
                    </c:if>
                </div>
            </div>

            <div class="d-flex align-items-center gap-3">
                <span class="badge badge-role px-3 py-2 rounded-pill">
                    <i class="bi bi-shield-check me-1"></i>
                    <c:out value="${sessionScope.loggedInUser.role}"/>
                </span>
                <div class="d-none sm:block d-md-block text-end">
                    <div class="fw-semibold small"><c:out value="${sessionScope.loggedInUser.fullName}"/></div>
                    <div class="text-muted" style="font-size: 0.75rem;"><c:out value="${sessionScope.loggedInUser.email}"/></div>
                </div>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-danger btn-sm">
                    <i class="bi bi-box-arrow-right me-1"></i>Logout
                </a>
            </div>
        </header>

        <main class="page-body">
            <c:if test="${not empty sessionScope.flashSuccess}">
                <div class="alert alert-success alert-dismissible fade show d-flex align-items-center" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    <div><c:out value="${sessionScope.flashSuccess}"/></div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="flashSuccess" scope="session"/>
            </c:if>

            <c:if test="${not empty sessionScope.flashError}">
                <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <div><c:out value="${sessionScope.flashError}"/></div>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
                <c:remove var="flashError" scope="session"/>
            </c:if>

