# Attendance Management System (MongoDB Edition)

A full-stack web-based **Attendance Management System** built with **Java (JDK 17+), Jakarta Servlets, JSP, JSTL, MongoDB (Sync Driver 5.14.0), BCrypt, and Bootstrap 5**, following the **MVC (Model–View–Controller)** architecture.

---

## 1. Key Features

### Authentication & Role-Based Access Control
- Secure session-based login and logout for **Admin**, **Teacher**, and **Student** roles.
- Passwords securely hashed with **BCrypt** (`jbcrypt`). Plaintext passwords are never stored.
- `AuthFilter` protects application routes, enforces role-based access permissions, and sends HTTP headers to prevent browser caching after logout.

### Admin Dashboard & Management
- Live KPI cards: Total Students, Total Teachers, Total Classes, Today's Attendance Rate (Present/Absent breakdown), and `< 75%` attendance shortage alerts.
- **Student Management**: Add, update, delete, search (by name, roll number, or department), filter by class, and optionally create a student portal login.
- **Teacher Management**: Add, update, delete, search faculty records, assign classes, and create teacher accounts.
- **Classes & Subjects Management**: Create/edit/delete classes and assign subjects to classes and teachers.

### Attendance Management (Core Module)
- Select Class, Subject, and Date to load the student roster.
- Mark students **Present** or **Absent** with optional remarks and quick **All Present / All Absent** buttons.
- **Duplicate Prevention**: MongoDB unique compound index `studentId + date + subjectId` plus DAO validation prevents duplicate attendance entries on the same date while allowing authorized updates.
- **Attendance History**: Filter by class, student, and date range, with inline status updates.

### Attendance Reports & 75% Threshold Highlighting
- **Monthly Report**, **Daily Report**, and **Overall Report** views.
- Formula: `Attendance Percentage = (Total Present Classes / Total Conducted Classes) × 100`.
- Automatically highlights students with attendance below **75%** in red, with a **Below 75% Only** filter and print support.

### Teacher & Student Dashboards
- **Teacher Dashboard**: View assigned classes, class student lists, class attendance percentages, and recent attendance logs.
- **Student Dashboard**: View personal total working days, present days, absent days, attendance percentage, `< 75%` shortage alert banner, and monthly/all-time history.

---

## 2. Technology Stack

- **Backend**: Java 17+, Jakarta Servlet 6.0, Jakarta JSTL 3.0
- **Database**: MongoDB Community Server 9.0 (database: `attendance_management`, URI: `mongodb://localhost:27017`)
- **Driver**: Official MongoDB Java Sync Driver 5.14.0 (`org.mongodb:mongodb-driver-sync`)
- **Security**: BCrypt password hashing (`org.mindrot:jbcrypt`)
- **Frontend**: JSP, Bootstrap 5.3, Bootstrap Icons, HTML5, CSS3, JavaScript
- **Server**: Apache Tomcat 10.1+ (Deployable WAR) & Embedded Tomcat 10 launcher for zero-setup execution
- **Build**: Apache Maven

---

## 3. MongoDB Collections & Indexes

Database name: `attendance_management`

1. **`users`**
   - Fields: `_id` (ObjectId), `username`, `password` (BCrypt), `role` (ADMIN/TEACHER/STUDENT), `fullName`, `email`, `active`, `createdAt`
   - Indexes: `username` (unique)
2. **`classes`**
   - Fields: `_id` (ObjectId), `className`, `section`, `department`, `academicYear`, `createdAt`
3. **`teachers`**
   - Fields: `_id` (ObjectId), `userId`, `employeeId`, `name`, `email`, `phone`, `department`, `assignedClassId`, `createdAt`
   - Indexes: `employeeId` (unique)
4. **`subjects`**
   - Fields: `_id` (ObjectId), `subjectCode`, `subjectName`, `classId`, `teacherId`, `createdAt`
   - Indexes: `subjectCode` (unique)
5. **`students`**
   - Fields: `_id` (ObjectId), `userId`, `name`, `rollNumber`, `email`, `phone`, `classId`, `department`, `createdAt`
   - Indexes: `rollNumber` (unique)
6. **`attendance`**
   - Fields: `_id` (ObjectId), `studentId`, `classId`, `subjectId`, `markedBy`, `date` (YYYY-MM-DD), `status` (Present/Absent/Holiday), `remarks`, `createdAt`, `updatedAt`
   - Indexes: `studentId + date + subjectId` (unique compound index), `classId + date`, `date`

---

## 4. How to Run the Project

### Option A: Run Directly with Embedded Tomcat 10 (Recommended & Fast)
No manual Tomcat installation needed!

1. Ensure MongoDB is running locally on port 27017:
   ```bash
   mongosh --eval "db.adminCommand('ping')"
   ```
2. Build the project and generate the runtime classpath:
   ```bash
   mvn compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt
   ```
3. Run the application:
   ```bash
   java -cp "target/classes:$(cat target/cp.txt)" com.attendance.Main
   ```
4. Open your browser and navigate to:
   ```
   http://localhost:8080/attendance-management/
   ```

---

### Option B: Deploy to External Apache Tomcat 10

1. Build the production WAR package:
   ```bash
   mvn clean package
   ```
2. Copy `target/attendance-management.war` into your Tomcat 10 `webapps/` folder:
   ```bash
   cp target/attendance-management.war /path/to/apache-tomcat-10/webapps/
   ```
3. Start Tomcat:
   ```bash
   /path/to/apache-tomcat-10/bin/startup.sh
   ```
4. Access:
   ```
   http://localhost:8080/attendance-management/
   ```

---

## 5. Sample Login Credentials

| Role | Username | Password | Profile / Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full administrative control, all classes & reports |
| **Teacher** | `teacher1` | `teacher123` | Prof. Aarav Sharma (Assigned to CSE Sem 5 - A) |
| **Teacher** | `teacher2` | `teacher123` | Prof. Priya Verma (Assigned to CSE Sem 5 - B) |
| **Student** | `student1` | `student123` | Rohan Gupta (`CS501`, ~100% attendance) |
| **Student** | `student3` | `student123` | Vikram Patel (`CS503`, `< 75%` shortage demo) |

*(Quick demo buttons are also available directly on the login page.)*

---

## 6. Running Automated Tests

Run the full test suite (16 tests verifying DAOs, BCrypt security, duplicate attendance prevention, percentage formulas, and AuthFilter):
```bash
mvn test
```
