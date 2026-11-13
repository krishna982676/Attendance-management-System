package com.attendance.model;

import java.util.Date;

/**
 * Represents a class/section document in the MongoDB `classes` collection.
 */
public class SchoolClass {
    private String classId;
    private String className;
    private String section;
    private String department;
    private String academicYear;
    private int studentCount;
    private Date createdAt;

    public SchoolClass() {}

    public SchoolClass(String className, String section, String department, String academicYear) {
        this.className = className;
        this.section = section;
        this.department = department;
        this.academicYear = academicYear;
    }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getDisplayName() {
        return className + " - " + section;
    }
}
