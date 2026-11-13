package com.attendance.dao;

import com.attendance.model.Attendance;
import com.attendance.model.AttendanceReportRow;
import com.attendance.model.Student;
import com.attendance.util.MongoDBConnection;
import com.attendance.util.ValidationUtil;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB Data Access Object for marking, updating, querying, and reporting attendance.
 */
public class AttendanceDAO {

    /**
     * Thrown when attempting to insert duplicate attendance for a student, date, and subject
     * without explicit update authorization.
     */
    public static class DuplicateAttendanceException extends Exception {
        public DuplicateAttendanceException(String message) {
            super(message);
        }
    }

    private MongoCollection<Document> collection() {
        return MongoDBConnection.getDatabase().getCollection("attendance");
    }

    public boolean hasAttendanceForClassSubjectAndDate(String classId, String subjectId, String date) {
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        if (classOid == null || ValidationUtil.isBlank(date)) {
            return false;
        }
        ObjectId subjectOid = ValidationUtil.parseObjectIdOrNull(subjectId);
        Bson filter = Filters.and(
                Filters.eq("classId", classOid),
                Filters.eq("date", date.trim()),
                subjectOid != null ? Filters.eq("subjectId", subjectOid) : Filters.eq("subjectId", null)
        );
        return collection().countDocuments(filter) > 0;
    }

    public Map<String, Attendance> getAttendanceMapForClassSubjectAndDate(String classId, String subjectId, String date) {
        Map<String, Attendance> map = new HashMap<>();
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        if (classOid == null || ValidationUtil.isBlank(date)) {
            return map;
        }
        ObjectId subjectOid = ValidationUtil.parseObjectIdOrNull(subjectId);
        Bson filter = Filters.and(
                Filters.eq("classId", classOid),
                Filters.eq("date", date.trim()),
                subjectOid != null ? Filters.eq("subjectId", subjectOid) : Filters.eq("subjectId", null)
        );
        for (Attendance a : queryAttendance(filter, 0)) {
            map.put(a.getStudentId(), a);
        }
        return map;
    }

    /**
     * Saves or updates attendance for students in a class on a given date and optional subject.
     * Prevents duplicate records using both pre-check and MongoDB unique compound index.
     */
    public int saveClassAttendance(String classId,
                                   String subjectId,
                                   String date,
                                   String markedByUserId,
                                   Map<String, String> studentStatuses,
                                   Map<String, String> studentRemarks,
                                   boolean allowUpdate) throws DuplicateAttendanceException {
        if (studentStatuses == null || studentStatuses.isEmpty()) {
            return 0;
        }
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        if (classOid == null || ValidationUtil.isBlank(date)) {
            throw new IllegalArgumentException("Valid classId and date are required.");
        }
        ObjectId subjectOid = ValidationUtil.parseObjectIdOrNull(subjectId);
        ObjectId markerOid = ValidationUtil.parseObjectIdOrNull(markedByUserId);
        String trimmedDate = date.trim();

        int affected = 0;
        for (Map.Entry<String, String> entry : studentStatuses.entrySet()) {
            ObjectId studentOid = ValidationUtil.parseObjectIdOrNull(entry.getKey());
            if (studentOid == null) {
                continue;
            }
            String status = normalizeStatus(entry.getValue());
            String remarks = studentRemarks != null ? ValidationUtil.trim(studentRemarks.get(entry.getKey())) : "";
            if (remarks.isEmpty()) {
                remarks = null;
            }

            Bson uniqueFilter = Filters.and(
                    Filters.eq("studentId", studentOid),
                    Filters.eq("date", trimmedDate),
                    subjectOid != null ? Filters.eq("subjectId", subjectOid) : Filters.eq("subjectId", null)
            );

            Document existing = collection().find(uniqueFilter).first();
            if (existing != null) {
                if (!allowUpdate) {
                    throw new DuplicateAttendanceException(
                            "Attendance has already been marked on " + trimmedDate + " for one or more students. Use Update mode to modify existing records.");
                }
                List<Bson> updates = new ArrayList<>();
                updates.add(Updates.set("classId", classOid));
                updates.add(Updates.set("status", status));
                updates.add(Updates.set("updatedAt", new Date()));
                if (markerOid != null) {
                    updates.add(Updates.set("markedBy", markerOid));
                }
                updates.add(remarks != null ? Updates.set("remarks", remarks) : Updates.unset("remarks"));
                collection().updateOne(Filters.eq("_id", existing.getObjectId("_id")), Updates.combine(updates));
                affected++;
            } else {
                Document doc = new Document("_id", new ObjectId())
                        .append("studentId", studentOid)
                        .append("classId", classOid)
                        .append("subjectId", subjectOid)
                        .append("markedBy", markerOid)
                        .append("date", trimmedDate)
                        .append("status", status)
                        .append("remarks", remarks)
                        .append("createdAt", new Date())
                        .append("updatedAt", new Date());
                try {
                    collection().insertOne(doc);
                    affected++;
                } catch (MongoWriteException ex) {
                    if (ex.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                        throw new DuplicateAttendanceException(
                                "Duplicate attendance prevented for date " + trimmedDate + ".");
                    }
                    throw ex;
                }
            }
        }
        return affected;
    }

    public boolean updateSingleAttendance(String attendanceId, String status, String remarks, String markedByUserId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(attendanceId);
        if (oid == null) {
            return false;
        }
        String normalized = normalizeStatus(status);
        ObjectId markerOid = ValidationUtil.parseObjectIdOrNull(markedByUserId);

        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("status", normalized));
        updates.add(Updates.set("updatedAt", new Date()));
        if (markerOid != null) {
            updates.add(Updates.set("markedBy", markerOid));
        }
        String cleanRemarks = ValidationUtil.trim(remarks);
        updates.add(!cleanRemarks.isEmpty() ? Updates.set("remarks", cleanRemarks) : Updates.unset("remarks"));

        UpdateResult result = collection().updateOne(Filters.eq("_id", oid), Updates.combine(updates));
        return result.getMatchedCount() > 0;
    }

    public Attendance findById(String attendanceId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(attendanceId);
        if (oid == null) {
            return null;
        }
        List<Attendance> list = queryAttendance(Filters.eq("_id", oid), 1);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Attendance> getAttendanceByStudent(String studentId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(studentId);
        if (oid == null) {
            return Collections.emptyList();
        }
        return queryAttendance(Filters.eq("studentId", oid), 0);
    }

    public List<Attendance> getAttendanceByClass(String classId) {
        ObjectId oid = ValidationUtil.parseObjectIdOrNull(classId);
        if (oid == null) {
            return Collections.emptyList();
        }
        return queryAttendance(Filters.eq("classId", oid), 0);
    }

    public List<Attendance> getAttendanceByDate(String date, String classId, String subjectId) {
        return getAttendanceHistory(classId, subjectId, null, date, date, 0);
    }

    public List<Attendance> getAttendanceHistory(String classId,
                                                 String subjectId,
                                                 String studentId,
                                                 String fromDate,
                                                 String toDate,
                                                 int limit) {
        List<Bson> filters = new ArrayList<>();
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        ObjectId subjectOid = ValidationUtil.parseObjectIdOrNull(subjectId);
        ObjectId studentOid = ValidationUtil.parseObjectIdOrNull(studentId);

        if (classOid != null) {
            filters.add(Filters.eq("classId", classOid));
        }
        if (subjectOid != null) {
            filters.add(Filters.eq("subjectId", subjectOid));
        }
        if (studentOid != null) {
            filters.add(Filters.eq("studentId", studentOid));
        }
        if (!ValidationUtil.isBlank(fromDate)) {
            filters.add(Filters.gte("date", fromDate.trim()));
        }
        if (!ValidationUtil.isBlank(toDate)) {
            filters.add(Filters.lte("date", toDate.trim()));
        }

        Bson combined = filters.isEmpty() ? new Document() : Filters.and(filters);
        return queryAttendance(combined, limit);
    }

    public List<Attendance> getDailyReport(String date, String classId) {
        return getAttendanceHistory(classId, null, null, date, date, 0);
    }

    public List<Attendance> getMonthlyAttendance(int year, int month, String classId, String studentId) {
        String prefix = String.format("%04d-%02d-", year, month);
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.regex("date", "^" + prefix));

        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        ObjectId studentOid = ValidationUtil.parseObjectIdOrNull(studentId);
        if (classOid != null) {
            filters.add(Filters.eq("classId", classOid));
        }
        if (studentOid != null) {
            filters.add(Filters.eq("studentId", studentOid));
        }
        return queryAttendance(Filters.and(filters), 0);
    }

    public List<AttendanceReportRow> getMonthlyReport(int year, int month, String classId, String studentId) {
        String prefix = String.format("%04d-%02d-", year, month);
        return buildReportRows(Filters.regex("date", "^" + prefix), classId, studentId);
    }

    public List<AttendanceReportRow> getOverallReport(String classId, String studentId) {
        return buildReportRows(null, classId, studentId);
    }

    public AttendanceReportRow getStudentOverallSummary(String studentId) {
        List<AttendanceReportRow> rows = getOverallReport(null, studentId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Attendance> getStudentMonthlyHistory(String studentId, Integer year, Integer month) {
        ObjectId studentOid = ValidationUtil.parseObjectIdOrNull(studentId);
        if (studentOid == null) {
            return Collections.emptyList();
        }
        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("studentId", studentOid));
        if (year != null && month != null) {
            String prefix = String.format("%04d-%02d-", year, month);
            filters.add(Filters.regex("date", "^" + prefix));
        }
        return queryAttendance(Filters.and(filters), 0);
    }

    /**
     * Calculates Attendance Percentage = (Total Present Classes / Total Conducted Classes) * 100.
     */
    public double calculateAttendancePercentage(int presentClasses, int conductedClasses) {
        if (conductedClasses <= 0) {
            return 0.0;
        }
        return Math.round(((presentClasses * 100.0) / conductedClasses) * 100.0) / 100.0;
    }

    /**
     * Returns {totalConducted, presentCount, absentCount} for the given date (YYYY-MM-DD).
     * Holidays are excluded from conducted counts.
     */
    public int[] getDateSummary(String date) {
        if (ValidationUtil.isBlank(date)) {
            return new int[]{0, 0, 0};
        }
        int present = 0;
        int absent = 0;
        for (Document doc : collection().find(Filters.eq("date", date.trim()))) {
            String status = doc.getString("status");
            if ("Present".equalsIgnoreCase(status)) {
                present++;
            } else if ("Absent".equalsIgnoreCase(status)) {
                absent++;
            }
        }
        return new int[]{present + absent, present, absent};
    }

    private List<AttendanceReportRow> buildReportRows(Bson dateFilter, String classId, String studentId) {
        StudentDAO studentDAO = new StudentDAO();
        List<Student> students;
        if (!ValidationUtil.isBlank(studentId)) {
            Student single = studentDAO.findById(studentId);
            students = single != null ? List.of(single) : Collections.emptyList();
        } else {
            students = studentDAO.findAll(null, classId);
        }

        if (students.isEmpty()) {
            return Collections.emptyList();
        }

        List<Bson> attFilters = new ArrayList<>();
        if (dateFilter != null) {
            attFilters.add(dateFilter);
        }
        ObjectId classOid = ValidationUtil.parseObjectIdOrNull(classId);
        ObjectId studentOid = ValidationUtil.parseObjectIdOrNull(studentId);
        if (classOid != null) {
            attFilters.add(Filters.eq("classId", classOid));
        }
        if (studentOid != null) {
            attFilters.add(Filters.eq("studentId", studentOid));
        }
        Bson combinedAttFilter = attFilters.isEmpty() ? new Document() : Filters.and(attFilters);

        Map<String, int[]> countsByStudent = new HashMap<>();
        for (Document doc : collection().find(combinedAttFilter)) {
            ObjectId sid = doc.getObjectId("studentId");
            if (sid == null) {
                continue;
            }
            String status = doc.getString("status");
            // Do not count holidays as conducted classes
            if ("Holiday".equalsIgnoreCase(status)) {
                continue;
            }
            int[] counts = countsByStudent.computeIfAbsent(sid.toHexString(), k -> new int[2]);
            if ("Present".equalsIgnoreCase(status)) {
                counts[0]++;
            } else if ("Absent".equalsIgnoreCase(status)) {
                counts[1]++;
            }
        }

        List<AttendanceReportRow> rows = new ArrayList<>();
        for (Student s : students) {
            int[] counts = countsByStudent.getOrDefault(s.getStudentId(), new int[]{0, 0});
            AttendanceReportRow row = new AttendanceReportRow();
            row.setStudentId(s.getStudentId());
            row.setRollNumber(s.getRollNumber());
            row.setStudentName(s.getName());
            row.setClassName(s.getClassName() != null ? s.getClassName() : "Unassigned");
            row.setDepartment(s.getDepartment());
            row.setPresentDays(counts[0]);
            row.setAbsentDays(counts[1]);
            row.setTotalDays(counts[0] + counts[1]);
            rows.add(row);
        }
        return rows;
    }

    private List<Attendance> queryAttendance(Bson filter, int limit) {
        MongoDatabase db = MongoDBConnection.getDatabase();
        Map<ObjectId, Document> studentMap = loadDocMap(db.getCollection("students"));
        Map<ObjectId, Document> classMap = loadDocMap(db.getCollection("classes"));
        Map<ObjectId, Document> subjectMap = loadDocMap(db.getCollection("subjects"));
        Map<ObjectId, Document> userMap = loadDocMap(db.getCollection("users"));

        FindIterable<Document> cursor = collection()
                .find(filter)
                .sort(Sorts.descending("date", "createdAt"));
        if (limit > 0) {
            cursor = cursor.limit(limit);
        }

        List<Attendance> list = new ArrayList<>();
        for (Document doc : cursor) {
            list.add(mapAttendance(doc, studentMap, classMap, subjectMap, userMap));
        }
        return list;
    }

    private Map<ObjectId, Document> loadDocMap(MongoCollection<Document> col) {
        Map<ObjectId, Document> map = new HashMap<>();
        for (Document d : col.find()) {
            ObjectId id = d.getObjectId("_id");
            if (id != null) {
                map.put(id, d);
            }
        }
        return map;
    }

    private Attendance mapAttendance(Document doc,
                                     Map<ObjectId, Document> studentMap,
                                     Map<ObjectId, Document> classMap,
                                     Map<ObjectId, Document> subjectMap,
                                     Map<ObjectId, Document> userMap) {
        Attendance a = new Attendance();
        ObjectId id = doc.getObjectId("_id");
        ObjectId sid = doc.getObjectId("studentId");
        ObjectId cid = doc.getObjectId("classId");
        ObjectId subId = doc.getObjectId("subjectId");
        ObjectId mbId = doc.getObjectId("markedBy");

        a.setAttendanceId(id != null ? id.toHexString() : null);
        a.setStudentId(sid != null ? sid.toHexString() : null);
        a.setClassId(cid != null ? cid.toHexString() : null);
        a.setSubjectId(subId != null ? subId.toHexString() : null);
        a.setMarkedBy(mbId != null ? mbId.toHexString() : null);
        a.setAttendanceDate(doc.getString("date"));
        a.setStatus(doc.getString("status"));
        a.setRemarks(doc.getString("remarks"));
        a.setCreatedAt(doc.getDate("createdAt"));
        a.setUpdatedAt(doc.getDate("updatedAt"));

        Document stuDoc = sid != null ? studentMap.get(sid) : null;
        if (stuDoc != null) {
            a.setStudentName(stuDoc.getString("name"));
            a.setRollNumber(stuDoc.getString("rollNumber"));
        }

        Document clsDoc = cid != null ? classMap.get(cid) : null;
        if (clsDoc != null) {
            a.setClassName(clsDoc.getString("className") + " - " + clsDoc.getString("section"));
        }

        Document subDoc = subId != null ? subjectMap.get(subId) : null;
        if (subDoc != null) {
            a.setSubjectName(subDoc.getString("subjectName"));
        }

        Document userDoc = mbId != null ? userMap.get(mbId) : null;
        if (userDoc != null) {
            a.setMarkedByName(userDoc.getString("fullName"));
        }
        return a;
    }

    private String normalizeStatus(String status) {
        if ("Absent".equalsIgnoreCase(status)) {
            return "Absent";
        }
        if ("Holiday".equalsIgnoreCase(status)) {
            return "Holiday";
        }
        return "Present";
    }
}
