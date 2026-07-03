package com.placepro.dao;

import com.placepro.service.report.AnalyticsSnapshot;
import com.placepro.service.report.ReportFilter;
import com.placepro.service.report.ReportTable;

public interface ReportDAO {

    ReportTable placementSummaryByDepartment(ReportFilter filter);

    ReportTable companySelectionStatistics(ReportFilter filter);

    ReportTable driveApplicantFunnel(ReportFilter filter);

    ReportTable studentPlacementRecords(ReportFilter filter);

    AnalyticsSnapshot analyticsSnapshot();
}
