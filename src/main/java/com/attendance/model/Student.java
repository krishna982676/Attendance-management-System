package com.attendance.model;

import java.util.Date;

/**
 * Represents a student document in the MongoDB `students` collection.
 */
public class Student {
    private String studentId;
    private String userId;
    private String name;
    private String rollNumber;
    private String email;
    private String phone;
    private String classId;
    private String className;
    private String department;
    private String username;
    private Date createdAt;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
