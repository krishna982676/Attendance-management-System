package com.attendance.model;

import java.util.Date;

/**
 * Represents a subject document in the MongoDB `subjects` collection.
 */
public class Subject {
    private String subjectId;
    private String subjectCode;
    private String subjectName;
    private String classId;
    private String teacherId;
    private String classDisplayName;
    private String teacherName;
    private Date createdAt;

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getClassDisplayName() { return classDisplayName; }
    public void setClassDisplayName(String classDisplayName) { this.classDisplayName = classDisplayName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
