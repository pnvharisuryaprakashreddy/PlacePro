package com.placepro.dao.impl;

import com.placepro.dao.ResumeDAO;
import com.placepro.model.Resume;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResumeDAOImpl extends AbstractJdbcDAO implements ResumeDAO {

    private static final String INSERT_SQL = "INSERT INTO resumes "
            + "(student_id, file_name, file_path, file_type, file_size_kb, is_current) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM resumes WHERE resume_id = ?";
    private static final String FIND_CURRENT_SQL = "SELECT * FROM resumes WHERE student_id = ? AND is_current = 1 ORDER BY uploaded_at DESC LIMIT 1";
    private static final String FIND_BY_STUDENT_SQL = "SELECT * FROM resumes WHERE student_id = ? ORDER BY uploaded_at DESC";
    private static final String UPDATE_SQL = "UPDATE resumes SET student_id = ?, file_name = ?, file_path = ?, file_type = ?, "
            + "file_size_kb = ?, is_current = ? WHERE resume_id = ?";
    private static final String DELETE_SQL = "DELETE FROM resumes WHERE resume_id = ?";

    @Override
    public Resume insert(Resume resume) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, resume);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    resume.setResumeId(generatedKeys.getInt(1));
                }
            }
            return resume;
        } catch (SQLException exception) {
            throw translateException("resume insert", exception);
        }
    }

    @Override
    public Optional<Resume> findById(int resumeId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, resumeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("resume lookup", exception);
        }
    }

    @Override
    public Optional<Resume> findCurrentByStudentId(int studentId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_CURRENT_SQL)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("current resume lookup", exception);
        }
    }

    @Override
    public List<Resume> findByStudentId(int studentId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_STUDENT_SQL)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Resume> resumes = new ArrayList<>();
                while (resultSet.next()) {
                    resumes.add(mapRow(resultSet));
                }
                return resumes;
            }
        } catch (SQLException exception) {
            throw translateException("resume list", exception);
        }
    }

    @Override
    public boolean update(Resume resume) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, resume);
            statement.setInt(7, resume.getResumeId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("resume update", exception);
        }
    }

    @Override
    public boolean deleteById(int resumeId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, resumeId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("resume delete", exception);
        }
    }

    private void bind(PreparedStatement statement, Resume resume) throws SQLException {
        statement.setInt(1, resume.getStudentId());
        statement.setString(2, resume.getFileName());
        statement.setString(3, resume.getFilePath());
        statement.setString(4, resume.getFileType());
        statement.setInt(5, resume.getFileSizeKb());
        statement.setBoolean(6, Boolean.TRUE.equals(resume.getIsCurrent()));
    }

    private Resume mapRow(ResultSet resultSet) throws SQLException {
        Resume resume = new Resume();
        resume.setResumeId(resultSet.getInt("resume_id"));
        resume.setStudentId(resultSet.getInt("student_id"));
        resume.setFileName(resultSet.getString("file_name"));
        resume.setFilePath(resultSet.getString("file_path"));
        resume.setFileType(resultSet.getString("file_type"));
        resume.setFileSizeKb(resultSet.getInt("file_size_kb"));
        resume.setIsCurrent(resultSet.getBoolean("is_current"));
        resume.setUploadedAt(resultSet.getTimestamp("uploaded_at").toLocalDateTime());
        return resume;
    }
}
