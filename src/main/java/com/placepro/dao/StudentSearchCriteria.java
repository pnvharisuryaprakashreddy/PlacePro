package com.placepro.dao;

import java.math.BigDecimal;

/**
 * Optional student search filters; null/blank fields are ignored.
 */
public class StudentSearchCriteria {

    /** Placement status filter values. */
    public static final String PLACED = "PLACED";
    public static final String NOT_PLACED = "NOT_PLACED";

    private final String name;
    private final String rollNumber;
    private final String branch;
    private final BigDecimal minCgpa;
    private final BigDecimal maxCgpa;
    private final String placementStatus;

    public StudentSearchCriteria(String name,
                                 String rollNumber,
                                 String branch,
                                 BigDecimal minCgpa,
                                 BigDecimal maxCgpa,
                                 String placementStatus) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.branch = branch;
        this.minCgpa = minCgpa;
        this.maxCgpa = maxCgpa;
        this.placementStatus = placementStatus;
    }

    public String getName() {
        return name;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getBranch() {
        return branch;
    }

    public BigDecimal getMinCgpa() {
        return minCgpa;
    }

    public BigDecimal getMaxCgpa() {
        return maxCgpa;
    }

    public String getPlacementStatus() {
        return placementStatus;
    }
}
