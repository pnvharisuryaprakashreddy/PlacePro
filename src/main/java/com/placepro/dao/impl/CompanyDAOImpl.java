package com.placepro.dao.impl;

import com.placepro.dao.CompanyDAO;
import com.placepro.model.Company;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyDAOImpl extends AbstractJdbcDAO implements CompanyDAO {

    private static final String INSERT_SQL = "INSERT INTO companies "
            + "(company_name, industry, contact_person, email, phone, website, address, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM companies WHERE company_id = ?";
    private static final String FIND_BY_NAME_SQL = "SELECT * FROM companies WHERE company_name = ?";
    private static final String FIND_ALL_ACTIVE_SQL = "SELECT * FROM companies WHERE is_active = 1 ORDER BY company_name";
    private static final String FIND_ALL_SQL = "SELECT * FROM companies ORDER BY company_name";
    private static final String SEARCH_SQL = "SELECT * FROM companies WHERE company_name LIKE ? ORDER BY company_name";
    private static final String UPDATE_SQL = "UPDATE companies SET company_name = ?, industry = ?, contact_person = ?, email = ?, "
            + "phone = ?, website = ?, address = ?, is_active = ? WHERE company_id = ?";
    private static final String DEACTIVATE_SQL = "UPDATE companies SET is_active = 0 WHERE company_id = ?";
    private static final String DELETE_SQL = "DELETE FROM companies WHERE company_id = ?";

    @Override
    public Company insert(Company company) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, company);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    company.setCompanyId(generatedKeys.getInt(1));
                }
            }
            return company;
        } catch (SQLException exception) {
            throw translateException("company insert", exception);
        }
    }

    @Override
    public Optional<Company> findById(int companyId) {
        return findSingle(FIND_BY_ID_SQL, companyId);
    }

    @Override
    public Optional<Company> findByCompanyName(String companyName) {
        return findSingle(FIND_BY_NAME_SQL, companyName);
    }

    @Override
    public List<Company> findAllActive() {
        return findMany(FIND_ALL_ACTIVE_SQL, null);
    }

    @Override
    public List<Company> findAll() {
        return findMany(FIND_ALL_SQL, null);
    }

    @Override
    public List<Company> searchByName(String keyword) {
        return findMany(SEARCH_SQL, "%" + keyword + "%");
    }

    @Override
    public List<Company> searchCompanies(String nameKeyword,
                                         String industryKeyword,
                                         String activeFilter,
                                         String driveFilter) {
        StringBuilder sql = new StringBuilder("SELECT c.* FROM companies c WHERE 1 = 1");
        List<Object> params = new ArrayList<>();

        if (nameKeyword != null && !nameKeyword.isBlank()) {
            sql.append(" AND c.company_name LIKE ?");
            params.add("%" + nameKeyword.trim() + "%");
        }
        if (industryKeyword != null && !industryKeyword.isBlank()) {
            sql.append(" AND c.industry LIKE ?");
            params.add("%" + industryKeyword.trim() + "%");
        }
        if ("ACTIVE".equalsIgnoreCase(activeFilter)) {
            sql.append(" AND c.is_active = 1");
        } else if ("INACTIVE".equalsIgnoreCase(activeFilter)) {
            sql.append(" AND c.is_active = 0");
        }
        if ("WITH_ACTIVE_DRIVES".equalsIgnoreCase(driveFilter)) {
            sql.append(" AND EXISTS (SELECT 1 FROM placement_drives d"
                    + " WHERE d.company_id = c.company_id AND d.status = 'PUBLISHED')");
        } else if ("WITHOUT_ACTIVE_DRIVES".equalsIgnoreCase(driveFilter)) {
            sql.append(" AND NOT EXISTS (SELECT 1 FROM placement_drives d"
                    + " WHERE d.company_id = c.company_id AND d.status = 'PUBLISHED')");
        }
        sql.append(" ORDER BY c.company_name");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int index = 0; index < params.size(); index++) {
                statement.setObject(index + 1, params.get(index));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Company> companies = new ArrayList<>();
                while (resultSet.next()) {
                    companies.add(mapRow(resultSet));
                }
                return companies;
            }
        } catch (SQLException exception) {
            throw translateException("company search", exception);
        }
    }

    @Override
    public boolean update(Company company) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, company);
            statement.setInt(9, company.getCompanyId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("company update", exception);
        }
    }

    @Override
    public boolean deactivate(int companyId) {
        return executeById(DEACTIVATE_SQL, "company deactivate", companyId);
    }

    @Override
    public boolean deleteById(int companyId) {
        return executeById(DELETE_SQL, "company delete", companyId);
    }

    private Optional<Company> findSingle(String sql, Object value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("company lookup", exception);
        }
    }

    private List<Company> findMany(String sql, String value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (value != null) {
                statement.setString(1, value);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Company> companies = new ArrayList<>();
                while (resultSet.next()) {
                    companies.add(mapRow(resultSet));
                }
                return companies;
            }
        } catch (SQLException exception) {
            throw translateException("company list", exception);
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

    private void bind(PreparedStatement statement, Company company) throws SQLException {
        statement.setString(1, company.getCompanyName());
        statement.setString(2, company.getIndustry());
        statement.setString(3, company.getContactPerson());
        statement.setString(4, company.getEmail());
        statement.setString(5, company.getPhone());
        statement.setString(6, company.getWebsite());
        statement.setString(7, company.getAddress());
        statement.setBoolean(8, Boolean.TRUE.equals(company.getIsActive()));
    }

    private Company mapRow(ResultSet resultSet) throws SQLException {
        Company company = new Company();
        company.setCompanyId(resultSet.getInt("company_id"));
        company.setCompanyName(resultSet.getString("company_name"));
        company.setIndustry(resultSet.getString("industry"));
        company.setContactPerson(resultSet.getString("contact_person"));
        company.setEmail(resultSet.getString("email"));
        company.setPhone(resultSet.getString("phone"));
        company.setWebsite(resultSet.getString("website"));
        company.setAddress(resultSet.getString("address"));
        company.setIsActive(resultSet.getBoolean("is_active"));
        company.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        company.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return company;
    }
}
