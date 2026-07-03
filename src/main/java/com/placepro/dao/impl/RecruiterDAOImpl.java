package com.placepro.dao.impl;

import com.placepro.dao.RecruiterDAO;
import com.placepro.model.Recruiter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecruiterDAOImpl extends AbstractJdbcDAO implements RecruiterDAO {

    private static final String INSERT_SQL = "INSERT INTO recruiters "
            + "(company_id, full_name, email, phone, password_hash, designation, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM recruiters WHERE recruiter_id = ?";
    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM recruiters WHERE email = ?";
    private static final String FIND_BY_COMPANY_SQL = "SELECT * FROM recruiters WHERE company_id = ? ORDER BY full_name";
    private static final String FIND_ALL_ACTIVE_SQL = "SELECT * FROM recruiters WHERE is_active = 1 ORDER BY full_name";
    private static final String UPDATE_SQL = "UPDATE recruiters SET company_id = ?, full_name = ?, email = ?, phone = ?, "
            + "password_hash = ?, designation = ?, is_active = ? WHERE recruiter_id = ?";
    private static final String DEACTIVATE_SQL = "UPDATE recruiters SET is_active = 0 WHERE recruiter_id = ?";
    private static final String DELETE_SQL = "DELETE FROM recruiters WHERE recruiter_id = ?";

    @Override
    public Recruiter insert(Recruiter recruiter) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, recruiter);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recruiter.setRecruiterId(generatedKeys.getInt(1));
                }
            }
            return recruiter;
        } catch (SQLException exception) {
            throw translateException("recruiter insert", exception);
        }
    }

    @Override
    public Optional<Recruiter> findById(int recruiterId) {
        return findSingle(FIND_BY_ID_SQL, recruiterId);
    }

    @Override
    public Optional<Recruiter> findByEmail(String email) {
        return findSingle(FIND_BY_EMAIL_SQL, email);
    }

    @Override
    public List<Recruiter> findByCompanyId(int companyId) {
        return findMany(FIND_BY_COMPANY_SQL, companyId);
    }

    @Override
    public List<Recruiter> findAllActive() {
        return findMany(FIND_ALL_ACTIVE_SQL, null);
    }

    @Override
    public boolean update(Recruiter recruiter) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, recruiter);
            statement.setInt(8, recruiter.getRecruiterId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("recruiter update", exception);
        }
    }

    @Override
    public boolean deactivate(int recruiterId) {
        return executeById(DEACTIVATE_SQL, "recruiter deactivate", recruiterId);
    }

    @Override
    public boolean deleteById(int recruiterId) {
        return executeById(DELETE_SQL, "recruiter delete", recruiterId);
    }

    private Optional<Recruiter> findSingle(String sql, Object value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("recruiter lookup", exception);
        }
    }

    private List<Recruiter> findMany(String sql, Integer companyId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (companyId != null) {
                statement.setInt(1, companyId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Recruiter> recruiters = new ArrayList<>();
                while (resultSet.next()) {
                    recruiters.add(mapRow(resultSet));
                }
                return recruiters;
            }
        } catch (SQLException exception) {
            throw translateException("recruiter list", exception);
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

    private void bind(PreparedStatement statement, Recruiter recruiter) throws SQLException {
        statement.setInt(1, recruiter.getCompanyId());
        statement.setString(2, recruiter.getFullName());
        statement.setString(3, recruiter.getEmail());
        statement.setString(4, recruiter.getPhone());
        statement.setString(5, recruiter.getPasswordHash());
        statement.setString(6, recruiter.getDesignation());
        statement.setBoolean(7, Boolean.TRUE.equals(recruiter.getIsActive()));
    }

    private Recruiter mapRow(ResultSet resultSet) throws SQLException {
        Recruiter recruiter = new Recruiter();
        recruiter.setRecruiterId(resultSet.getInt("recruiter_id"));
        recruiter.setCompanyId(resultSet.getInt("company_id"));
        recruiter.setFullName(resultSet.getString("full_name"));
        recruiter.setEmail(resultSet.getString("email"));
        recruiter.setPhone(resultSet.getString("phone"));
        recruiter.setPasswordHash(resultSet.getString("password_hash"));
        recruiter.setDesignation(resultSet.getString("designation"));
        recruiter.setIsActive(resultSet.getBoolean("is_active"));
        recruiter.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        recruiter.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return recruiter;
    }
}
