package com.placepro.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class InterviewSchedule {

    private Integer interviewId;
    private Integer applicationId;
    private Integer roundNumber;
    private String roundType;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private String venue;
    private String outcome;
    private String notes;
    private Integer createdByOfficerId;
    private Integer createdByRecruiterId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Integer interviewId) {
        this.interviewId = interviewId;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getRoundType() {
        return roundType;
    }

    public void setRoundType(String roundType) {
        this.roundType = roundType;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getCreatedByOfficerId() {
        return createdByOfficerId;
    }

    public void setCreatedByOfficerId(Integer createdByOfficerId) {
        this.createdByOfficerId = createdByOfficerId;
    }

    public Integer getCreatedByRecruiterId() {
        return createdByRecruiterId;
    }

    public void setCreatedByRecruiterId(Integer createdByRecruiterId) {
        this.createdByRecruiterId = createdByRecruiterId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
