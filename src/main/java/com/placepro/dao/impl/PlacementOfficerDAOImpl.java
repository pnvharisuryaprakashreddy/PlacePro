package com.placepro.dao.impl;

import com.placepro.dao.PlacementOfficerDAO;
import com.placepro.model.PlacementOfficer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlacementOfficerDAOImpl extends AbstractJdbcDAO implements PlacementOfficerDAO {

    private static final String INSERT_SQL = "INSERT INTO placement_officers "
            + "(employee_id, full_name, email, phone, password_hash, role, department, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM placement_officers WHERE officer_id = ?";
    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM placement_officers WHERE email = ?";
    private static final String FIND_BY_EMPLOYEE_ID_SQL = "SELECT * FROM placement_officers WHERE employee_id = ?";
    private static final String FIND_ALL_ACTIVE_SQL = "SELECT * FROM placement_officers WHERE is_active = 1 ORDER BY full_name";
    private static final String FIND_ALL_SQL = "SELECT * FROM placement_officers ORDER BY full_name";
    private static final String FIND_BY_ROLE_SQL = "SELECT * FROM placement_officers WHERE role = ? ORDER BY full_name";
    private static final String UPDATE_SQL = "UPDATE placement_officers SET employee_id = ?, full_name = ?, email = ?, phone = ?, "
            + "password_hash = ?, role = ?, department = ?, is_active = ? WHERE officer_id = ?";
    private static final String DEACTIVATE_SQL = "UPDATE placement_officers SET is_active = 0 WHERE officer_id = ?";
    private static final String DELETE_SQL = "DELETE FROM placement_officers WHERE officer_id = ?";

    @Override
    public PlacementOfficer insert(PlacementOfficer officer) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, officer);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    officer.setOfficerId(generatedKeys.getInt(1));
                }
            }
            return officer;
        } catch (SQLException exception) {
            throw translateException("placement officer insert", exception);
        }
    }

    @Override
    public Optional<PlacementOfficer> findById(int officerId) {
        return findSingle(FIND_BY_ID_SQL, officerId);
    }

    @Override
    public Optional<PlacementOfficer> findByEmail(String email) {
        return findSingle(FIND_BY_EMAIL_SQL, email);
    }

    @Override
    public Optional<PlacementOfficer> findByEmployeeId(String employeeId) {
        return findSingle(FIND_BY_EMPLOYEE_ID_SQL, employeeId);
    }

    @Override
    public List<PlacementOfficer> findAllActive() {
        return findMany(FIND_ALL_ACTIVE_SQL, null);
    }

    @Override
    public List<PlacementOfficer> findAll() {
        return findMany(FIND_ALL_SQL, null);
    }

    @Override
    public List<PlacementOfficer> findByRole(String role) {
        return findMany(FIND_BY_ROLE_SQL, role);
    }

    @Override
    public boolean update(PlacementOfficer officer) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, officer);
            statement.setInt(9, officer.getOfficerId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("placement officer update", exception);
        }
    }

    @Override
    public boolean deactivate(int officerId) {
        return executeById(DEACTIVATE_SQL, "placement officer deactivate", officerId);
    }

    @Override
    public boolean deleteById(int officerId) {
        return executeById(DELETE_SQL, "placement officer delete", officerId);
    }

    private Optional<PlacementOfficer> findSingle(String sql, Object value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("placement officer lookup", exception);
        }
    }

    private List<PlacementOfficer> findMany(String sql, String role) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (role != null) {
                statement.setString(1, role);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<PlacementOfficer> officers = new ArrayList<>();
                while (resultSet.next()) {
                    officers.add(mapRow(resultSet));
                }
                return officers;
            }
        } catch (SQLException exception) {
            throw translateException("placement officer list", exception);
        }
    }

    private boolean executeById(String sql, String operation, int id) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException(operation, exception);
        }
    }

    private void bind(PreparedStatement statement, PlacementOfficer officer) throws SQLException {
        statement.setString(1, officer.getEmployeeId());
        statement.setString(2, officer.getFullName());
        statement.setString(3, officer.getEmail());
        statement.setString(4, officer.getPhone());
        statement.setString(5, officer.getPasswordHash());
        statement.setString(6, officer.getRole());
        statement.setString(7, officer.getDepartment());
        statement.setBoolean(8, Boolean.TRUE.equals(officer.getIsActive()));
    }

    private PlacementOfficer mapRow(ResultSet resultSet) throws SQLException {
        PlacementOfficer officer = new PlacementOfficer();
        officer.setOfficerId(resultSet.getInt("officer_id"));
        officer.setEmployeeId(resultSet.getString("employee_id"));
        officer.setFullName(resultSet.getString("full_name"));
        officer.setEmail(resultSet.getString("email"));
        officer.setPhone(resultSet.getString("phone"));
        officer.setPasswordHash(resultSet.getString("password_hash"));
        officer.setRole(resultSet.getString("role"));
        officer.setDepartment(resultSet.getString("department"));
        officer.setIsActive(resultSet.getBoolean("is_active"));
        officer.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        officer.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return officer;
    }
}
