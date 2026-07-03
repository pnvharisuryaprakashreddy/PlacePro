package com.placepro.dao.impl;

import com.placepro.dao.ReportDAO;
import com.placepro.service.report.AnalyticsSnapshot;
import com.placepro.service.report.DepartmentPlacementSummary;
import com.placepro.service.report.ReportFilter;
import com.placepro.service.report.ReportTable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aggregation queries for reports and analytics. All queries run read-only
 * and apply the optional {@link ReportFilter} fields dynamically.
 */
public class ReportDAOImpl extends AbstractJdbcDAO implements ReportDAO {

    @Override
    public ReportTable placementSummaryByDepartment(ReportFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.branch, "
                        + "COUNT(DISTINCT s.student_id) AS total_students, "
                        + "COUNT(DISTINCT CASE WHEN ad.status = 'SELECTED' THEN s.student_id END) AS placed_students, "
                        + "AVG(CASE WHEN ad.status = 'SELECTED' THEN COALESCE(ad.package_max, ad.package_min) END) AS avg_package "
                        + "FROM students s "
                        + "LEFT JOIN (SELECT a.student_id, a.status, a.applied_at, d.company_id, d.package_min, d.package_max "
                        + "           FROM applications a JOIN placement_drives d ON d.drive_id = a.drive_id) ad "
                        + "       ON ad.student_id = s.student_id");
        List<Object> params = new ArrayList<>();
        appendJoinCondition(sql, params, "ad.company_id = ?", filter.getCompanyId());
        appendDateRangeToJoin(sql, params, "ad.applied_at", filter);
        sql.append(" WHERE s.is_active = 1");
        appendWhereCondition(sql, params, "s.graduation_year = ?", filter.getGraduationYear());
        appendWhereCondition(sql, params, "s.branch = ?", filter.getBranch());
        sql.append(" GROUP BY s.branch ORDER BY s.branch");

        List<List<Object>> rows = new ArrayList<>();
        query(sql.toString(), params, resultSet -> {
            while (resultSet.next()) {
                int total = resultSet.getInt("total_students");
                int placed = resultSet.getInt("placed_students");
                BigDecimal avgPackage = resultSet.getBigDecimal("avg_package");
                rows.add(Arrays.asList(
                        resultSet.getString("branch"),
                        total,
                        placed,
                        percentage(placed, total),
                        avgPackage == null ? "-" : avgPackage.setScale(2, RoundingMode.HALF_UP)));
            }
        }, "department placement summary");

        return new ReportTable(
                "Placement Summary by Department",
                Arrays.asList("Department", "Total Students", "Students Placed", "Placement %", "Avg Package (LPA)"),
                rows);
    }

    @Override
    public ReportTable companySelectionStatistics(ReportFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.company_name, "
                        + "COUNT(DISTINCT d.drive_id) AS drives, "
                        + "COUNT(DISTINCT a.application_id) AS applications, "
                        + "COUNT(DISTINCT CASE WHEN a.status IN ('SHORTLISTED','INTERVIEW_SCHEDULED','SELECTED') THEN a.application_id END) AS shortlisted, "
                        + "COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN a.application_id END) AS selected, "
                        + "AVG(CASE WHEN a.status = 'SELECTED' THEN COALESCE(d.package_max, d.package_min) END) AS avg_package "
                        + "FROM companies c "
                        + "JOIN placement_drives d ON d.company_id = c.company_id "
                        + "LEFT JOIN applications a ON a.drive_id = d.drive_id");
        List<Object> params = new ArrayList<>();
        appendDateRangeToJoin(sql, params, "a.applied_at", filter);
        appendStudentSubfilterToJoin(sql, params, filter);
        sql.append(" WHERE 1 = 1");
        appendWhereCondition(sql, params, "c.company_id = ?", filter.getCompanyId());
        sql.append(" GROUP BY c.company_id, c.company_name ORDER BY selected DESC, c.company_name");

        List<List<Object>> rows = new ArrayList<>();
        query(sql.toString(), params, resultSet -> {
            while (resultSet.next()) {
                int applications = resultSet.getInt("applications");
                int selected = resultSet.getInt("selected");
                BigDecimal avgPackage = resultSet.getBigDecimal("avg_package");
                rows.add(Arrays.asList(
                        resultSet.getString("company_name"),
                        resultSet.getInt("drives"),
                        applications,
                        resultSet.getInt("shortlisted"),
                        selected,
                        percentage(selected, applications),
                        avgPackage == null ? "-" : avgPackage.setScale(2, RoundingMode.HALF_UP)));
            }
        }, "company selection statistics");

        return new ReportTable(
                "Company-wise Selection Statistics",
                Arrays.asList("Company", "Drives", "Applications", "Shortlisted", "Selected",
                        "Selection %", "Avg Package (LPA)"),
                rows);
    }

    @Override
    public ReportTable driveApplicantFunnel(ReportFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT d.drive_id, c.company_name, d.job_title, d.status AS drive_status, "
                        + "COUNT(DISTINCT a.application_id) AS applied, "
                        + "COUNT(DISTINCT CASE WHEN a.status IN ('SHORTLISTED','INTERVIEW_SCHEDULED','SELECTED') THEN a.application_id END) AS shortlisted, "
                        + "COUNT(DISTINCT CASE WHEN i.interview_id IS NOT NULL THEN a.application_id END) AS interviewed, "
                        + "COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN a.application_id END) AS selected "
                        + "FROM placement_drives d "
                        + "JOIN companies c ON c.company_id = d.company_id "
                        + "LEFT JOIN applications a ON a.drive_id = d.drive_id");
        List<Object> params = new ArrayList<>();
        appendDateRangeToJoin(sql, params, "a.applied_at", filter);
        appendStudentSubfilterToJoin(sql, params, filter);
        sql.append(" LEFT JOIN interview_schedule i ON i.application_id = a.application_id");
        sql.append(" WHERE 1 = 1");
        appendWhereCondition(sql, params, "d.company_id = ?", filter.getCompanyId());
        sql.append(" GROUP BY d.drive_id, c.company_name, d.job_title, d.status ORDER BY d.drive_id");

        List<List<Object>> rows = new ArrayList<>();
        query(sql.toString(), params, resultSet -> {
            while (resultSet.next()) {
                rows.add(Arrays.asList(
                        resultSet.getInt("drive_id"),
                        resultSet.getString("company_name"),
                        resultSet.getString("job_title"),
                        resultSet.getString("drive_status"),
                        resultSet.getInt("applied"),
                        resultSet.getInt("shortlisted"),
                        resultSet.getInt("interviewed"),
                        resultSet.getInt("selected")));
            }
        }, "drive applicant funnel");

        return new ReportTable(
                "Drive-wise Applicant Funnel",
                Arrays.asList("Drive ID", "Company", "Job Title", "Drive Status",
                        "Applied", "Shortlisted", "Interviewed", "Selected"),
                rows);
    }

    @Override
    public ReportTable studentPlacementRecords(ReportFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT s.roll_number, s.full_name, s.branch, s.graduation_year, s.cgpa, "
                        + "COUNT(DISTINCT a.application_id) AS applications, "
                        + "COUNT(DISTINCT i.interview_id) AS interviews, "
                        + "MAX(CASE WHEN a.status = 'SELECTED' THEN c.company_name END) AS placed_company, "
                        + "MAX(CASE WHEN a.status = 'SELECTED' THEN COALESCE(d.package_max, d.package_min) END) AS package_lpa "
                        + "FROM students s "
                        + "LEFT JOIN applications a ON a.student_id = s.student_id");
        List<Object> params = new ArrayList<>();
        appendDateRangeToJoin(sql, params, "a.applied_at", filter);
        if (filter.getCompanyId() != null) {
            sql.append(" AND a.drive_id IN (SELECT drive_id FROM placement_drives WHERE company_id = ?)");
            params.add(filter.getCompanyId());
        }
        sql.append(" LEFT JOIN placement_drives d ON d.drive_id = a.drive_id")
                .append(" LEFT JOIN companies c ON c.company_id = d.company_id")
                .append(" LEFT JOIN interview_schedule i ON i.application_id = a.application_id")
                .append(" WHERE s.is_active = 1");
        appendWhereCondition(sql, params, "s.graduation_year = ?", filter.getGraduationYear());
        appendWhereCondition(sql, params, "s.branch = ?", filter.getBranch());
        sql.append(" GROUP BY s.student_id, s.roll_number, s.full_name, s.branch, s.graduation_year, s.cgpa")
                .append(" ORDER BY s.roll_number");

        List<List<Object>> rows = new ArrayList<>();
        query(sql.toString(), params, resultSet -> {
            while (resultSet.next()) {
                String placedCompany = resultSet.getString("placed_company");
                BigDecimal packageLpa = resultSet.getBigDecimal("package_lpa");
                rows.add(Arrays.asList(
                        resultSet.getString("roll_number"),
                        resultSet.getString("full_name"),
                        resultSet.getString("branch"),
                        resultSet.getInt("graduation_year"),
                        resultSet.getBigDecimal("cgpa"),
                        resultSet.getInt("applications"),
                        resultSet.getInt("interviews"),
                        placedCompany == null ? "Not Placed" : placedCompany,
                        packageLpa == null ? "-" : packageLpa.setScale(2, RoundingMode.HALF_UP)));
            }
        }, "student placement records");

        return new ReportTable(
                "Individual Student Placement Record",
                Arrays.asList("Roll Number", "Name", "Branch", "Grad Year", "CGPA",
                        "Applications", "Interviews", "Placed At", "Package (LPA)"),
                rows);
    }

    @Override
    public AnalyticsSnapshot analyticsSnapshot() {
        int[] kpis = new int[3];
        BigDecimal[] avgPackage = new BigDecimal[1];
        query("SELECT COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN a.student_id END) AS total_placed, "
                        + "COUNT(DISTINCT a.application_id) AS total_applications, "
                        + "COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN a.application_id END) AS total_selected, "
                        + "AVG(CASE WHEN a.status = 'SELECTED' THEN COALESCE(d.package_max, d.package_min) END) AS avg_package "
                        + "FROM applications a JOIN placement_drives d ON d.drive_id = a.drive_id",
                new ArrayList<>(), resultSet -> {
                    if (resultSet.next()) {
                        kpis[0] = resultSet.getInt("total_placed");
                        kpis[1] = resultSet.getInt("total_applications");
                        kpis[2] = resultSet.getInt("total_selected");
                        avgPackage[0] = resultSet.getBigDecimal("avg_package");
                    }
                }, "analytics KPIs");

        int[] totalStudents = new int[1];
        query("SELECT COUNT(*) AS total FROM students WHERE is_active = 1",
                new ArrayList<>(), resultSet -> {
                    if (resultSet.next()) {
                        totalStudents[0] = resultSet.getInt("total");
                    }
                }, "active student count");

        List<DepartmentPlacementSummary> departments = new ArrayList<>();
        query("SELECT s.branch, "
                        + "COUNT(DISTINCT s.student_id) AS total_students, "
                        + "COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN s.student_id END) AS placed_students "
                        + "FROM students s LEFT JOIN applications a ON a.student_id = s.student_id "
                        + "WHERE s.is_active = 1 GROUP BY s.branch ORDER BY s.branch",
                new ArrayList<>(), resultSet -> {
                    while (resultSet.next()) {
                        departments.add(new DepartmentPlacementSummary(
                                resultSet.getString("branch"),
                                resultSet.getInt("total_students"),
                                resultSet.getInt("placed_students")));
                    }
                }, "department placement analytics");

        List<AnalyticsSnapshot.CompanySelectionCount> topCompanies = new ArrayList<>();
        query("SELECT c.company_name, COUNT(DISTINCT a.application_id) AS selected "
                        + "FROM applications a "
                        + "JOIN placement_drives d ON d.drive_id = a.drive_id "
                        + "JOIN companies c ON c.company_id = d.company_id "
                        + "WHERE a.status = 'SELECTED' "
                        + "GROUP BY c.company_id, c.company_name ORDER BY selected DESC, c.company_name LIMIT 5",
                new ArrayList<>(), resultSet -> {
                    while (resultSet.next()) {
                        topCompanies.add(new AnalyticsSnapshot.CompanySelectionCount(
                                resultSet.getString("company_name"),
                                resultSet.getInt("selected")));
                    }
                }, "top recruiting companies");

        return new AnalyticsSnapshot(
                kpis[0],
                totalStudents[0],
                kpis[1],
                kpis[2],
                avgPackage[0],
                departments,
                topCompanies);
    }

    private void appendJoinCondition(StringBuilder sql, List<Object> params, String condition, Object value) {
        if (value != null) {
            sql.append(" AND ").append(condition);
            params.add(value);
        }
    }

    private void appendWhereCondition(StringBuilder sql, List<Object> params, String condition, Object value) {
        if (value != null) {
            sql.append(" AND ").append(condition);
            params.add(value);
        }
    }

    /** Appends applied_at range checks to the current JOIN's ON clause. */
    private void appendDateRangeToJoin(StringBuilder sql, List<Object> params, String column, ReportFilter filter) {
        if (filter.getFromDate() != null) {
            sql.append(" AND ").append(column).append(" >= ?");
            params.add(Timestamp.valueOf(filter.getFromDate().atStartOfDay()));
        }
        if (filter.getToDate() != null) {
            sql.append(" AND ").append(column).append(" < ?");
            params.add(Timestamp.valueOf(filter.getToDate().plusDays(1).atStartOfDay()));
        }
    }

    /** Restricts joined applications to students matching branch / graduation year filters. */
    private void appendStudentSubfilterToJoin(StringBuilder sql, List<Object> params, ReportFilter filter) {
        if (filter.getBranch() == null && filter.getGraduationYear() == null) {
            return;
        }
        sql.append(" AND a.student_id IN (SELECT student_id FROM students WHERE 1 = 1");
        if (filter.getBranch() != null) {
            sql.append(" AND branch = ?");
            params.add(filter.getBranch());
        }
        if (filter.getGraduationYear() != null) {
            sql.append(" AND graduation_year = ?");
            params.add(filter.getGraduationYear());
        }
        sql.append(")");
    }

    private static String percentage(int part, int whole) {
        if (whole == 0) {
            return "0.0%";
        }
        return String.format(java.util.Locale.ENGLISH, "%.1f%%", 100.0 * part / whole);
    }

    @FunctionalInterface
    private interface ResultSetConsumer {
        void accept(ResultSet resultSet) throws SQLException;
    }

    private void query(String sql, List<Object> params, ResultSetConsumer consumer, String operation) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < params.size(); index++) {
                statement.setObject(index + 1, params.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                consumer.accept(resultSet);
            }
        } catch (SQLException exception) {
            throw translateException(operation, exception);
        }
    }
}
