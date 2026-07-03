package com.placepro.service.report;

public class DepartmentPlacementSummary {

    private final String branch;
    private int registeredStudents;
    private int selectedCount;

    public DepartmentPlacementSummary(String branch, int registeredStudents, int selectedCount) {
        this.branch = branch;
        this.registeredStudents = registeredStudents;
        this.selectedCount = selectedCount;
    }

    public String getBranch() {
        return branch;
    }

    public int getRegisteredStudents() {
        return registeredStudents;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    void incrementRegisteredStudents() {
        registeredStudents++;
    }

    void incrementSelectedCount() {
        selectedCount++;
    }
}
