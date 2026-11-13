<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | Attendance Management System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="login-page">

<div class="card login-card">
    <div class="row g-0">
        <!-- Left Hero Panel -->
        <div class="col-lg-5 login-hero d-flex flex-column justify-content-between">
            <div>
                <div class="d-flex align-items-center gap-3 mb-4">
                    <img src="${pageContext.request.contextPath}/images/logo.svg" alt="AMS Logo" width="44" height="44">
                    <div>
                        <h5 class="mb-0 fw-bold">AttendancePro</h5>
                        <small class="text-white-50">Academic Attendance Portal</small>
                    </div>
                </div>
                <h2 class="fw-bold mb-3">Smart Attendance Tracking for Modern Campuses</h2>
                <p class="text-white-50 mb-4">
                    Streamline daily attendance, manage student &amp; faculty records, prevent duplicate entries,
                    and monitor 75% eligibility thresholds effortlessly.
                </p>
            </div>

            <div>
                <div class="small text-uppercase fw-semibold text-white-50 mb-2">
                    <i class="bi bi-lightning-charge-fill me-1"></i>Quick Demo Login
                </div>
                <div class="d-grid gap-2">
                    <button type="button" class="demo-cred-btn" onclick="fillDemoCredentials('admin', 'admin123')">
                        <div class="fw-semibold">Admin Account</div>
                        <div class="text-white-50">admin / admin123</div>
                    </button>
                    <button type="button" class="demo-cred-btn" onclick="fillDemoCredentials('teacher1', 'teacher123')">
                        <div class="fw-semibold">Teacher Account (CSE-A)</div>
                        <div class="text-white-50">teacher1 / teacher123</div>
                    </button>
                    <button type="button" class="demo-cred-btn" onclick="fillDemoCredentials('student1', 'student123')">
                        <div class="fw-semibold">Student Account (CS501)</div>
                        <div class="text-white-50">student1 / student123</div>
                    </button>
                </div>
            </div>
        </div>

        <!-- Right Login Form -->
        <div class="col-lg-7 bg-white p-4 p-md-5 d-flex flex-column justify-content-center">
            <div class="mb-4">
                <h3 class="fw-bold mb-1">Welcome Back</h3>
                <p class="text-muted mb-0">Sign in with your institutional credentials to continue.</p>
            </div>

            <c:if test="${param.loggedOut == 'true'}">
                <div class="alert alert-success d-flex align-items-center" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>
                    <div>You have been securely logged out.</div>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger d-flex align-items-center" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <div><c:out value="${error}"/></div>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
                <div class="mb-3">
                    <label for="username" class="form-label fw-semibold">Username</label>
                    <div class="input-group input-group-lg">
                        <span class="input-group-text bg-light"><i class="bi bi-person text-muted"></i></span>
                        <input type="text" class="form-control" id="username" name="username"
                               value="<c:out value='${username}'/>" placeholder="Enter username" required autofocus>
                    </div>
                </div>

                <div class="mb-4">
                    <label for="password" class="form-label fw-semibold">Password</label>
                    <div class="input-group input-group-lg">
                        <span class="input-group-text bg-light"><i class="bi bi-lock text-muted"></i></span>
                        <input type="password" class="form-control" id="password" name="password"
                               placeholder="Enter password" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100 fw-semibold">
                    <i class="bi bi-box-arrow-in-right me-2"></i>Sign In
                </button>
            </form>

            <div class="mt-4 pt-3 border-top text-center text-muted small">
                Role-based access enabled for Administrators, Teachers, and Students.
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>

