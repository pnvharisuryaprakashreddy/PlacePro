package com.placepro.dao;

import com.placepro.model.Application;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ApplicationDAO {

    Application insert(Application application);

    Application insert(Connection connection, Application application);

    Optional<Application> findById(int applicationId);

    Optional<Application> findByStudentAndDrive(int studentId, int driveId);

    List<Application> findByStudentId(int studentId);

    List<Application> findByDriveId(int driveId);

    List<Application> findByStatus(String status);

    boolean update(Application application);

    boolean update(Connection connection, Application application);

    boolean deleteById(int applicationId);
}
