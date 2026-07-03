package com.placepro.dao;

import com.placepro.model.InterviewSchedule;

import java.util.List;
import java.util.Optional;

public interface InterviewScheduleDAO {

    InterviewSchedule insert(InterviewSchedule interviewSchedule);

    Optional<InterviewSchedule> findById(int interviewId);

    Optional<InterviewSchedule> findByApplicationAndRound(int applicationId, int roundNumber);

    List<InterviewSchedule> findByApplicationId(int applicationId);

    boolean update(InterviewSchedule interviewSchedule);

    boolean deleteById(int interviewId);
}
