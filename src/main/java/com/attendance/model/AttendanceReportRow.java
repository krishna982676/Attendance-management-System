package com.attendance.model;

/**
 * Summary row for student attendance reports with percentage and <75% indicator.
 */
public class AttendanceReportRow {
    private String studentId;
    private String rollNumber;
    private String studentName;
    private String className;
    private String department;
    private int totalDays;
    private int presentDays;
    private int absentDays;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getPresentDays() { return presentDays; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }

    public int getAbsentDays() { return absentDays; }
    public void setAbsentDays(int absentDays) { this.absentDays = absentDays; }

    /**
     * Attendance Percentage = (Total Present Classes / Total Conducted Classes) * 100
     */
    public double getAttendancePercentage() {
        if (totalDays <= 0) {
            return 0.0;
        }
        return Math.round(((presentDays * 100.0) / totalDays) * 100.0) / 100.0;
    }

    public boolean isBelowThreshold() {
        return totalDays > 0 && getAttendancePercentage() < 75.0;
    }
}
