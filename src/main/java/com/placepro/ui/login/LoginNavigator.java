package com.placepro.ui.login;

import com.placepro.model.PlacementOfficer;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;

public interface LoginNavigator {

    void showSelection();

    void showStudentLogin();

    void showStudentRegistration();

    void showOfficerLogin();

    void showAdminLogin();

    void showRecruiterLogin();

    void showStudentDashboard(Student student);

    void showOfficerDashboard(PlacementOfficer officer);

    void showAdminDashboard(PlacementOfficer admin);

    void showRecruiterDashboard(Recruiter recruiter);
}
