package com.placepro.dao.impl;

import com.placepro.dao.StudentDAO;
import com.placepro.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAOImpl extends AbstractJdbcDAO implements StudentDAO {

    private static final String INSERT_SQL = "INSERT INTO students "
            + "(roll_number, full_name, email, phone, password_hash, branch, cgpa, backlog_count, graduation_year, is_active) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM students WHERE student_id = ?";
    private static final String FIND_BY_EMAIL_SQL = "SELECT * FROM students WHERE email = ?";
    private static final String FIND_BY_ROLL_NUMBER_SQL = "SELECT * FROM students WHERE roll_number = ?";
    private static final String FIND_ALL_ACTIVE_SQL = "SELECT * FROM students WHERE is_active = 1 ORDER BY full_name";
    private static final String FIND_ALL_SQL = "SELECT * FROM students ORDER BY full_name";
    private static final String SEARCH_SQL = "SELECT * FROM students WHERE full_name LIKE ? OR roll_number LIKE ? ORDER BY full_name";
    private static final String UPDATE_SQL = "UPDATE students SET roll_number = ?, full_name = ?, email = ?, phone = ?, "
            + "password_hash = ?, branch = ?, cgpa = ?, backlog_count = ?, graduation_year = ?, is_active = ? WHERE student_id = ?";
    private static final String DEACTIVATE_SQL = "UPDATE students SET is_active = 0 WHERE student_id = ?";
    private static final String DELETE_SQL = "DELETE FROM students WHERE student_id = ?";

    @Override
    public Student insert(Student student) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bindStudent(statement, student);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setStudentId(generatedKeys.getInt(1));
                }
            }
            return student;
        } catch (SQLException exception) {
            throw translateException("student insert", exception);
        }
    }

    @Override
    public Optional<Student> findById(int studentId) {
        return findSingle(FIND_BY_ID_SQL, studentId);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return findSingle(FIND_BY_EMAIL_SQL, email);
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        return findSingle(FIND_BY_ROLL_NUMBER_SQL, rollNumber);
    }

    @Override
    public List<Student> findAllActive() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_ACTIVE_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return mapStudents(resultSet);
        } catch (SQLException exception) {
            throw translateException("student list", exception);
        }
    }

    @Override
    public List<Student> findAll() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return mapStudents(resultSet);
        } catch (SQLException exception) {
            throw translateException("student list", exception);
        }
    }

    @Override
    public List<Student> searchByNameOrRollNumber(String keyword) {
        String searchTerm = "%" + keyword + "%";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH_SQL)) {
            statement.setString(1, searchTerm);
            statement.setString(2, searchTerm);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapStudents(resultSet);
            }
        } catch (SQLException exception) {
            throw translateException("student search", exception);
        }
    }

    @Override
    public boolean update(Student student) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bindStudent(statement, student);
            statement.setInt(11, student.getStudentId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("student update", exception);
        }
    }

    @Override
    public boolean deactivate(int studentId) {
        return executeBooleanUpdate(DEACTIVATE_SQL, "student deactivate", studentId);
    }

    @Override
    public boolean deleteById(int studentId) {
        return executeBooleanUpdate(DELETE_SQL, "student delete", studentId);
    }

    private Optional<Student> findSingle(String sql, Object value) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("student lookup", exception);
        }
    }

    private boolean executeBooleanUpdate(String sql, String operation, int studentId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException(operation, exception);
        }
    }

    private void bindStudent(PreparedStatement statement, Student student) throws SQLException {
        statement.setString(1, student.getRollNumber());
        statement.setString(2, student.getFullName());
        statement.setString(3, student.getEmail());
        statement.setString(4, student.getPhone());
        statement.setString(5, student.getPasswordHash());
        statement.setString(6, student.getBranch());
        statement.setBigDecimal(7, student.getCgpa());
        statement.setInt(8, student.getBacklogCount());
        statement.setInt(9, student.getGraduationYear());
        statement.setBoolean(10, Boolean.TRUE.equals(student.getIsActive()));
    }

    private List<Student> mapStudents(ResultSet resultSet) throws SQLException {
        List<Student> students = new ArrayList<>();
        while (resultSet.next()) {
            students.add(mapRow(resultSet));
        }
        return students;
    }

    private Student mapRow(ResultSet resultSet) throws SQLException {
        Student student = new Student();
        student.setStudentId(resultSet.getInt("student_id"));
        student.setRollNumber(resultSet.getString("roll_number"));
        student.setFullName(resultSet.getString("full_name"));
        student.setEmail(resultSet.getString("email"));
        student.setPhone(resultSet.getString("phone"));
        student.setPasswordHash(resultSet.getString("password_hash"));
        student.setBranch(resultSet.getString("branch"));
        student.setCgpa(resultSet.getBigDecimal("cgpa"));
        student.setBacklogCount(resultSet.getInt("backlog_count"));
        student.setGraduationYear(resultSet.getInt("graduation_year"));
        student.setIsActive(resultSet.getBoolean("is_active"));
        student.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        student.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return student;
    }
}
