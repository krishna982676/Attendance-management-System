package com.attendance.model;

import java.util.Date;

/**
 * Represents an authenticated user account (ADMIN, TEACHER, or STUDENT) stored in MongoDB.
 */
public class User {
    private String userId;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private boolean active;
    private Date createdAt;

    public User() {}

    public User(String userId, String username, String role, String fullName, String email, boolean active) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isTeacher() { return "TEACHER".equalsIgnoreCase(role); }
    public boolean isStudent() { return "STUDENT".equalsIgnoreCase(role); }
}
