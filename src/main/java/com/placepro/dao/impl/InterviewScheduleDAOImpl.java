package com.placepro.dao.impl;

import com.placepro.dao.InterviewScheduleDAO;
import com.placepro.model.InterviewSchedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterviewScheduleDAOImpl extends AbstractJdbcDAO implements InterviewScheduleDAO {

    private static final String INSERT_SQL = "INSERT INTO interview_schedule "
            + "(application_id, round_number, round_type, scheduled_date, scheduled_time, venue, outcome, notes, "
            + "created_by_officer_id, created_by_recruiter_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM interview_schedule WHERE interview_id = ?";
    private static final String FIND_BY_APP_AND_ROUND_SQL = "SELECT * FROM interview_schedule WHERE application_id = ? AND round_number = ?";
    private static final String FIND_BY_APPLICATION_SQL = "SELECT * FROM interview_schedule WHERE application_id = ? ORDER BY round_number";
    private static final String FIND_BY_COMPANY_SQL = "SELECT i.* FROM interview_schedule i "
            + "INNER JOIN applications a ON a.application_id = i.application_id "
            + "INNER JOIN placement_drives d ON d.drive_id = a.drive_id "
            + "WHERE d.company_id = ? ORDER BY i.scheduled_date, i.scheduled_time, i.round_number";
    private static final String UPDATE_SQL = "UPDATE interview_schedule SET application_id = ?, round_number = ?, round_type = ?, "
            + "scheduled_date = ?, scheduled_time = ?, venue = ?, outcome = ?, notes = ?, created_by_officer_id = ?, "
            + "created_by_recruiter_id = ? WHERE interview_id = ?";
    private static final String DELETE_SQL = "DELETE FROM interview_schedule WHERE interview_id = ?";

    @Override
    public InterviewSchedule insert(InterviewSchedule interviewSchedule) {
        try (Connection connection = getConnection()) {
            return insert(connection, interviewSchedule);
        } catch (SQLException exception) {
            throw translateException("interview schedule insert", exception);
        }
    }

    @Override
    public InterviewSchedule insert(Connection connection, InterviewSchedule interviewSchedule) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, interviewSchedule);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    interviewSchedule.setInterviewId(generatedKeys.getInt(1));
                }
            }
            return interviewSchedule;
        } catch (SQLException exception) {
            throw translateException("interview schedule insert", exception);
        }
    }

    @Override
    public Optional<InterviewSchedule> findById(int interviewId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, interviewId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("interview schedule lookup", exception);
        }
    }

    @Override
    public Optional<InterviewSchedule> findByApplicationAndRound(int applicationId, int roundNumber) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_APP_AND_ROUND_SQL)) {
            statement.setInt(1, applicationId);
            statement.setInt(2, roundNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("interview schedule lookup by round", exception);
        }
    }

    @Override
    public List<InterviewSchedule> findByApplicationId(int applicationId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_APPLICATION_SQL)) {
            statement.setInt(1, applicationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<InterviewSchedule> schedules = new ArrayList<>();
                while (resultSet.next()) {
                    schedules.add(mapRow(resultSet));
                }
                return schedules;
            }
        } catch (SQLException exception) {
            throw translateException("interview schedule list", exception);
        }
    }

    @Override
    public List<InterviewSchedule> findByCompanyId(int companyId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_COMPANY_SQL)) {
            statement.setInt(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<InterviewSchedule> schedules = new ArrayList<>();
                while (resultSet.next()) {
                    schedules.add(mapRow(resultSet));
                }
                return schedules;
            }
        } catch (SQLException exception) {
            throw translateException("interview schedule list by company", exception);
        }
    }

    @Override
    public boolean update(InterviewSchedule interviewSchedule) {
        try (Connection connection = getConnection()) {
            return update(connection, interviewSchedule);
        } catch (SQLException exception) {
            throw translateException("interview schedule update", exception);
        }
    }

    @Override
    public boolean update(Connection connection, InterviewSchedule interviewSchedule) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, interviewSchedule);
            statement.setInt(11, interviewSchedule.getInterviewId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("interview schedule update", exception);
        }
    }

    @Override
    public boolean deleteById(int interviewId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, interviewId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("interview schedule delete", exception);
        }
    }

    private void bind(PreparedStatement statement, InterviewSchedule interviewSchedule) throws SQLException {
        statement.setInt(1, interviewSchedule.getApplicationId());
        statement.setInt(2, interviewSchedule.getRoundNumber());
        statement.setString(3, interviewSchedule.getRoundType());
        statement.setObject(4, interviewSchedule.getScheduledDate());
        statement.setObject(5, interviewSchedule.getScheduledTime());
        statement.setString(6, interviewSchedule.getVenue());
        statement.setString(7, interviewSchedule.getOutcome());
        statement.setString(8, interviewSchedule.getNotes());
        if (interviewSchedule.getCreatedByOfficerId() == null) {
            statement.setNull(9, java.sql.Types.INTEGER);
        } else {
            statement.setInt(9, interviewSchedule.getCreatedByOfficerId());
        }
        if (interviewSchedule.getCreatedByRecruiterId() == null) {
            statement.setNull(10, java.sql.Types.INTEGER);
        } else {
            statement.setInt(10, interviewSchedule.getCreatedByRecruiterId());
        }
    }

    private InterviewSchedule mapRow(ResultSet resultSet) throws SQLException {
        InterviewSchedule schedule = new InterviewSchedule();
        schedule.setInterviewId(resultSet.getInt("interview_id"));
        schedule.setApplicationId(resultSet.getInt("application_id"));
        schedule.setRoundNumber(resultSet.getInt("round_number"));
        schedule.setRoundType(resultSet.getString("round_type"));
        schedule.setScheduledDate(resultSet.getObject("scheduled_date", java.time.LocalDate.class));
        schedule.setScheduledTime(resultSet.getObject("scheduled_time", java.time.LocalTime.class));
        schedule.setVenue(resultSet.getString("venue"));
        schedule.setOutcome(resultSet.getString("outcome"));
        schedule.setNotes(resultSet.getString("notes"));
        int officerId = resultSet.getInt("created_by_officer_id");
        schedule.setCreatedByOfficerId(resultSet.wasNull() ? null : officerId);
        int recruiterId = resultSet.getInt("created_by_recruiter_id");
        schedule.setCreatedByRecruiterId(resultSet.wasNull() ? null : recruiterId);
        schedule.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        schedule.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        return schedule;
    }
}
