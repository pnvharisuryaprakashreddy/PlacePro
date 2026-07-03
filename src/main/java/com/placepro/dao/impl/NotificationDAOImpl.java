package com.placepro.dao.impl;

import com.placepro.dao.NotificationDAO;
import com.placepro.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationDAOImpl extends AbstractJdbcDAO implements NotificationDAO {

    private static final String INSERT_SQL = "INSERT INTO notifications "
            + "(student_id, officer_id, recruiter_id, title, message, notification_type, reference_id, is_read) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = "SELECT * FROM notifications WHERE notification_id = ?";
    private static final String FIND_BY_STUDENT_SQL = "SELECT * FROM notifications WHERE student_id = ? ORDER BY created_at DESC";
    private static final String FIND_BY_OFFICER_SQL = "SELECT * FROM notifications WHERE officer_id = ? ORDER BY created_at DESC";
    private static final String FIND_BY_RECRUITER_SQL = "SELECT * FROM notifications WHERE recruiter_id = ? ORDER BY created_at DESC";
    private static final String FIND_UNREAD_BY_STUDENT_SQL = "SELECT * FROM notifications WHERE student_id = ? AND is_read = 0 ORDER BY created_at DESC";
    private static final String UPDATE_SQL = "UPDATE notifications SET student_id = ?, officer_id = ?, recruiter_id = ?, title = ?, "
            + "message = ?, notification_type = ?, reference_id = ?, is_read = ? WHERE notification_id = ?";
    private static final String DELETE_SQL = "DELETE FROM notifications WHERE notification_id = ?";

    @Override
    public Notification insert(Notification notification) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, notification);
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setNotificationId(generatedKeys.getInt(1));
                }
            }
            return notification;
        } catch (SQLException exception) {
            throw translateException("notification insert", exception);
        }
    }

    @Override
    public Optional<Notification> findById(int notificationId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, notificationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw translateException("notification lookup", exception);
        }
    }

    @Override
    public List<Notification> findByStudentId(int studentId) {
        return findMany(FIND_BY_STUDENT_SQL, "student notifications", studentId);
    }

    @Override
    public List<Notification> findByOfficerId(int officerId) {
        return findMany(FIND_BY_OFFICER_SQL, "officer notifications", officerId);
    }

    @Override
    public List<Notification> findByRecruiterId(int recruiterId) {
        return findMany(FIND_BY_RECRUITER_SQL, "recruiter notifications", recruiterId);
    }

    @Override
    public List<Notification> findUnreadByStudentId(int studentId) {
        return findMany(FIND_UNREAD_BY_STUDENT_SQL, "unread student notifications", studentId);
    }

    @Override
    public boolean update(Notification notification) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            bind(statement, notification);
            statement.setInt(9, notification.getNotificationId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("notification update", exception);
        }
    }

    @Override
    public boolean deleteById(int notificationId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setInt(1, notificationId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw translateException("notification delete", exception);
        }
    }

    private List<Notification> findMany(String sql, String operation, int recipientId) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recipientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Notification> notifications = new ArrayList<>();
                while (resultSet.next()) {
                    notifications.add(mapRow(resultSet));
                }
                return notifications;
            }
        } catch (SQLException exception) {
            throw translateException(operation, exception);
        }
    }

    private void bind(PreparedStatement statement, Notification notification) throws SQLException {
        if (notification.getStudentId() == null) {
            statement.setNull(1, java.sql.Types.INTEGER);
        } else {
            statement.setInt(1, notification.getStudentId());
        }
        if (notification.getOfficerId() == null) {
            statement.setNull(2, java.sql.Types.INTEGER);
        } else {
            statement.setInt(2, notification.getOfficerId());
        }
        if (notification.getRecruiterId() == null) {
            statement.setNull(3, java.sql.Types.INTEGER);
        } else {
            statement.setInt(3, notification.getRecruiterId());
        }
        statement.setString(4, notification.getTitle());
        statement.setString(5, notification.getMessage());
        statement.setString(6, notification.getNotificationType());
        if (notification.getReferenceId() == null) {
            statement.setNull(7, java.sql.Types.INTEGER);
        } else {
            statement.setInt(7, notification.getReferenceId());
        }
        statement.setBoolean(8, Boolean.TRUE.equals(notification.getIsRead()));
    }

    private Notification mapRow(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(resultSet.getInt("notification_id"));
        int studentId = resultSet.getInt("student_id");
        notification.setStudentId(resultSet.wasNull() ? null : studentId);
        int officerId = resultSet.getInt("officer_id");
        notification.setOfficerId(resultSet.wasNull() ? null : officerId);
        int recruiterId = resultSet.getInt("recruiter_id");
        notification.setRecruiterId(resultSet.wasNull() ? null : recruiterId);
        notification.setTitle(resultSet.getString("title"));
        notification.setMessage(resultSet.getString("message"));
        notification.setNotificationType(resultSet.getString("notification_type"));
        int referenceId = resultSet.getInt("reference_id");
        notification.setReferenceId(resultSet.wasNull() ? null : referenceId);
        notification.setIsRead(resultSet.getBoolean("is_read"));
        notification.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return notification;
    }
}
