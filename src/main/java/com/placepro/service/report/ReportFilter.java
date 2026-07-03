package com.placepro.service.report;

import java.time.LocalDate;

/**
 * Optional filters shared by all reports. A null field means "no filter".
 */
public class ReportFilter {

    private final Integer graduationYear;
    private final String branch;
    private final Integer companyId;
    private final LocalDate fromDate;
    private final LocalDate toDate;

    public ReportFilter(Integer graduationYear,
                        String branch,
                        Integer companyId,
                        LocalDate fromDate,
                        LocalDate toDate) {
        this.graduationYear = graduationYear;
        this.branch = branch;
        this.companyId = companyId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static ReportFilter none() {
        return new ReportFilter(null, null, null, null, null);
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public String getBranch() {
        return branch;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }
}
