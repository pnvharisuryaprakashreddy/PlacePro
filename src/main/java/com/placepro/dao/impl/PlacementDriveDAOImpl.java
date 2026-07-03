package com.placepro.dao.impl;

import com.placepro.dao.PlacementDriveDAO;
import com.placepro.model.PlacementDrive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlacementDriveDAOImpl extends AbstractJdbcDAO implements PlacementDriveDAO {

    private static final String INSERT_SQL = "INSERT INTO placement_drives "
            + "(company_id, job_title, job_description, package_min, package_max, min_cgpa, max_backlogs, allowed_branches, "
            + "visit_date, application_deadline, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM placement_drives WHERE drive_id = ?";
    private static final String FIND_BY_COMPANY_SQL = "SELECT * FROM placement_drives WHERE company_id = ? ORDER BY application_deadline DESC";
    private static final String FIND_BY_STATUS_SQL = "SELECT * FROM placement_drives WHERE status = ? ORDER BY application_deadline DESC";
    private static final String FIND_PUBLISHED_SQL = "SELECT * FROM placement_drives WHERE status = 'PUBLISHED' ORDER BY application_deadline ASC";
    private static final String UPDATE_SQL = "UPDATE placement_drives SET company_id = ?, job_title = ?, job_description = ?, package_min = ?, "
            + "package_max = ?, min_cgpa = ?, max_backlogs = ?, allowed_branches = ?, visit_date = ?, application_deadline = ?, "
            + "status = ?, created_by = ? WHERE drive_id = ?";
    private static final String DELETE_SQL = "DELETE FROM placement_drives WHERE drive_id = ?";

    @Override
    public PlacementDrive insert(PlacementDrive drive) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, drive);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    drive.setDriveId(generatedKeys.getInt(1));
                }
            }
            return drive;
        } catch (SQLException exception) {
            throw translateException("placement drive insert", exception);
        }
    }

    @Override
    public Optional<PlacementDrive> findById(int driveId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, driveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("placement drive lookup", exception);
        }
    }

    @Override
    public List<PlacementDrive> findByCompanyId(int companyId) {
        return findMany(FIND_BY_COMPANY_SQL, companyId);
    }

    @Override
    public List<PlacementDrive> findByStatus(String status) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STATUS_SQL)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        } catch (SQLException exception) {
            throw translateException("placement drive list by status", exception);
        }
    }

    @Override
    public List<PlacementDrive> findPublishedDrives() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_PUBLISHED_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return mapList(resultSet);
        } catch (SQLException exception) {
            throw translateException("published drive list", exception);
        }
    }

    @Override
    public boolean update(PlacementDrive drive) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, drive);
            statement.setInt(13, drive.getDriveId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("placement drive update", exception);
        }
    }

    @Override
    public boolean deleteById(int driveId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, driveId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("placement drive delete", exception);
        }
    }

    private List<PlacementDrive> findMany(String sql, int companyId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        } catch (SQLException exception) {
            throw translateException("placement drive list", exception);
        }
    }

    private void bind(PreparedStatement statement, PlacementDrive drive) throws SQLException {
        statement.setInt(1, drive.getCompanyId());
        statement.setString(2, drive.getJobTitle());
        statement.setString(3, drive.getJobDescription());
        statement.setBigDecimal(4, drive.getPackageMin());
        statement.setBigDecimal(5, drive.getPackageMax());
        statement.setBigDecimal(6, drive.getMinCgpa());
        statement.setInt(7, drive.getMaxBacklogs());
        statement.setString(8, drive.getAllowedBranches());
        statement.setObject(9, drive.getVisitDate());
        statement.setObject(10, drive.getApplicationDeadline());
        statement.setString(11, drive.getStatus());
        statement.setInt(12, drive.getCreatedBy());
    }

    private List<PlacementDrive> mapList(ResultSet resultSet) throws SQLException {
        List<PlacementDrive> drives = new ArrayList<>();
        while (resultSet.next()) {
            drives.add(mapRow(resultSet));
        }
        return drives;
    }

    private PlacementDrive mapRow(ResultSet resultSet) throws SQLException {
        PlacementDrive drive = new PlacementDrive();
        drive.setDriveId(resultSet.getInt("drive_id"));
        drive.setCompanyId(resultSet.getInt("company_id"));
        drive.setJobTitle(resultSet.getString("job_title"));
        drive.setJobDescription(resultSet.getString("job_description"));
        drive.setPackageMin(resultSet.getBigDecimal("package_min"));
        drive.setPackageMax(resultSet.getBigDecimal("package_max"));
        drive.setMinCgpa(resultSet.getBigDecimal("min_cgpa"));
        drive.setMaxBacklogs(resultSet.getInt("max_backlogs"));
        drive.setAllowedBranches(resultSet.getString("allowed_branches"));
        drive.setVisitDate(resultSet.getObject("visit_date", java.time.LocalDate.class));
        drive.setApplicationDeadline(resultSet.getTimestamp("application_deadline").toLocalDateTime());
        drive.setStatus(resultSet.getString("status"));
        drive.setCreatedBy(resultSet.getInt("created_by"));
        drive.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        drive.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return drive;
    }
}
