package com.placepro.service.report;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * KPI snapshot for the analytics dashboard, computed live from the database.
 */
public class AnalyticsSnapshot {

    public static final class CompanySelectionCount {
        private final String companyName;
        private final int selectedCount;

        public CompanySelectionCount(String companyName, int selectedCount) {
            this.companyName = companyName;
            this.selectedCount = selectedCount;
        }

        public String getCompanyName() {
            return companyName;
        }

        public int getSelectedCount() {
            return selectedCount;
        }
    }

    private final int totalPlacements;
    private final int totalActiveStudents;
    private final int totalApplications;
    private final int totalSelectedApplications;
    private final BigDecimal averagePackage;
    private final List<DepartmentPlacementSummary> departmentSummaries;
    private final List<CompanySelectionCount> topCompanies;

    public AnalyticsSnapshot(int totalPlacements,
                             int totalActiveStudents,
                             int totalApplications,
                             int totalSelectedApplications,
                             BigDecimal averagePackage,
                             List<DepartmentPlacementSummary> departmentSummaries,
                             List<CompanySelectionCount> topCompanies) {
        this.totalPlacements = totalPlacements;
        this.totalActiveStudents = totalActiveStudents;
        this.totalApplications = totalApplications;
        this.totalSelectedApplications = totalSelectedApplications;
        this.averagePackage = averagePackage;
        this.departmentSummaries = Collections.unmodifiableList(departmentSummaries);
        this.topCompanies = Collections.unmodifiableList(topCompanies);
    }

    public int getTotalPlacements() {
        return totalPlacements;
    }

    public int getTotalActiveStudents() {
        return totalActiveStudents;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public int getTotalSelectedApplications() {
        return totalSelectedApplications;
    }

    public BigDecimal getAveragePackage() {
        return averagePackage;
    }

    public double getOverallPlacementPercentage() {
        return totalActiveStudents == 0 ? 0.0 : 100.0 * totalPlacements / totalActiveStudents;
    }

    public double getConversionRate() {
        return totalApplications == 0 ? 0.0 : 100.0 * totalSelectedApplications / totalApplications;
    }

    public List<DepartmentPlacementSummary> getDepartmentSummaries() {
        return departmentSummaries;
    }

    public List<CompanySelectionCount> getTopCompanies() {
        return topCompanies;
    }
}
