package com.placepro.dao;

import com.placepro.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationDAO {

    Notification insert(Notification notification);

    Optional<Notification> findById(int notificationId);

    List<Notification> findByStudentId(int studentId);

    List<Notification> findByOfficerId(int officerId);

    List<Notification> findByRecruiterId(int recruiterId);

    List<Notification> findUnreadByStudentId(int studentId);

    boolean update(Notification notification);

    boolean deleteById(int notificationId);
}
