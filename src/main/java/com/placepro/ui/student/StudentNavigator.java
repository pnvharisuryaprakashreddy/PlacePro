package com.placepro.ui.student;

import com.placepro.service.student.StudentDriveSummary;

public interface StudentNavigator {

    void showDashboard();

    void showBrowseDrives();

    void showMyApplications();

    void showProfileResume();

    void showDriveDetail(StudentDriveSummary driveSummary);
}
