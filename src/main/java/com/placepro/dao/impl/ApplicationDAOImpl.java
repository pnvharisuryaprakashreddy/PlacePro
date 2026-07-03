package com.placepro.dao.impl;

import com.placepro.dao.ApplicationDAO;
import com.placepro.model.Application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationDAOImpl extends AbstractJdbcDAO implements ApplicationDAO {

    private static final String INSERT_SQL = "INSERT INTO applications (student_id, drive_id, resume_id, status) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM applications WHERE application_id = ?";
    private static final String FIND_BY_STUDENT_DRIVE_SQL = "SELECT * FROM applications WHERE student_id = ? AND drive_id = ?";
    private static final String FIND_BY_STUDENT_SQL = "SELECT * FROM applications WHERE student_id = ? ORDER BY applied_at DESC";
    private static final String FIND_BY_DRIVE_SQL = "SELECT * FROM applications WHERE drive_id = ? ORDER BY applied_at DESC";
    private static final String FIND_BY_STATUS_SQL = "SELECT * FROM applications WHERE status = ? ORDER BY applied_at DESC";
    private static final String UPDATE_SQL = "UPDATE applications SET student_id = ?, drive_id = ?, resume_id = ?, status = ? WHERE application_id = ?";
    private static final String DELETE_SQL = "DELETE FROM applications WHERE application_id = ?";

    @Override
    public Application insert(Application application) {
        try (Connection connection = getConnection()) {
            return insert(connection, application);
        } catch (SQLException exception) {
            throw translateException("application insert", exception);
        }
    }

    @Override
    public Application insert(Connection connection, Application application) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, application);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    application.setApplicationId(generatedKeys.getInt(1));
                }
            }
            return application;
        } catch (SQLException exception) {
            throw translateException("application insert", exception);
        }
    }

    @Override
    public Optional<Application> findById(int applicationId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("application lookup", exception);
        }
    }

    @Override
    public Optional<Application> findByStudentAndDrive(int studentId, int driveId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STUDENT_DRIVE_SQL)) {
            statement.setInt(1, studentId);
            statement.setInt(2, driveId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("application lookup by student and drive", exception);
        }
    }

    @Override
    public List<Application> findByStudentId(int studentId) {
        return findMany(FIND_BY_STUDENT_SQL, studentId);
    }

    @Override
    public List<Application> findByDriveId(int driveId) {
        return findMany(FIND_BY_DRIVE_SQL, driveId);
    }

    @Override
    public List<Application> findByStatus(String status) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STATUS_SQL)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        } catch (SQLException exception) {
            throw translateException("application list by status", exception);
        }
    }

    @Override
    public boolean update(Application application) {
        try (Connection connection = getConnection()) {
            return update(connection, application);
        } catch (SQLException exception) {
            throw translateException("application update", exception);
        }
    }

    @Override
    public boolean update(Connection connection, Application application) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, application);
            statement.setInt(5, application.getApplicationId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("application update", exception);
        }
    }

    @Override
    public boolean deleteById(int applicationId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, applicationId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("application delete", exception);
        }
    }

    private List<Application> findMany(String sql, int value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapList(resultSet);
            }
        } catch (SQLException exception) {
            throw translateException("application list", exception);
        }
    }

    private void bind(PreparedStatement statement, Application application) throws SQLException {
        statement.setInt(1, application.getStudentId());
        statement.setInt(2, application.getDriveId());
        if (application.getResumeId() == null) {
            statement.setNull(3, java.sql.Types.INTEGER);
        } else {
            statement.setInt(3, application.getResumeId());
        }
        statement.setString(4, application.getStatus());
    }

    private List<Application> mapList(ResultSet resultSet) throws SQLException {
        List<Application> applications = new ArrayList<>();
        while (resultSet.next()) {
            applications.add(mapRow(resultSet));
        }
        return applications;
    }

    private Application mapRow(ResultSet resultSet) throws SQLException {
        Application application = new Application();
        application.setApplicationId(resultSet.getInt("application_id"));
        application.setStudentId(resultSet.getInt("student_id"));
        application.setDriveId(resultSet.getInt("drive_id"));
        int resumeId = resultSet.getInt("resume_id");
        application.setResumeId(resultSet.wasNull() ? null : resumeId);
        application.setStatus(resultSet.getString("status"));
        application.setAppliedAt(resultSet.getTimestamp("applied_at").toLocalDateTime());
        application.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return application;
    }
}
