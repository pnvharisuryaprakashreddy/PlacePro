package com.placepro.dao;

import com.placepro.model.Student;

/**
 * One student search result plus the company they were placed at (null if not placed),
 * resolved in the same query to avoid N+1 lookups.
 */
public class StudentSearchRow {

    private final Student student;
    private final String placedCompany;

    public StudentSearchRow(Student student, String placedCompany) {
        this.student = student;
        this.placedCompany = placedCompany;
    }

    public Student getStudent() {
        return student;
    }

    public String getPlacedCompany() {
        return placedCompany;
    }
}
