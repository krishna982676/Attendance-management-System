package com.attendance.model;

import java.util.Date;

/**
 * Represents a teacher document in the MongoDB `teachers` collection.
 */
public class Teacher {
    private String teacherId;
    private String userId;
    private String employeeId;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String assignedClassId;
    private String assignedClassName;
    private String username;
    private Date createdAt;

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAssignedClassId() { return assignedClassId; }
    public void setAssignedClassId(String assignedClassId) { this.assignedClassId = assignedClassId; }

    public String getAssignedClassName() { return assignedClassName; }
    public void setAssignedClassName(String assignedClassName) { this.assignedClassName = assignedClassName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
