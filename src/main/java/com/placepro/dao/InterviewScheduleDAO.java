package com.placepro.dao;

import com.placepro.model.InterviewSchedule;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface InterviewScheduleDAO {

    InterviewSchedule insert(InterviewSchedule interviewSchedule);

    InterviewSchedule insert(Connection connection, InterviewSchedule interviewSchedule);

    Optional<InterviewSchedule> findById(int interviewId);

    Optional<InterviewSchedule> findByApplicationAndRound(int applicationId, int roundNumber);

    List<InterviewSchedule> findByApplicationId(int applicationId);

    List<InterviewSchedule> findByCompanyId(int companyId);

    boolean update(InterviewSchedule interviewSchedule);

    boolean update(Connection connection, InterviewSchedule interviewSchedule);

    boolean deleteById(int interviewId);
}
