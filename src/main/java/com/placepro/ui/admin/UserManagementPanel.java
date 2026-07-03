package com.placepro.ui.admin;

import com.placepro.model.PlacementOfficer;
import com.placepro.model.Recruiter;
import com.placepro.model.Student;
import com.placepro.service.ServiceException;
import com.placepro.service.admin.UserManagementService;
import com.placepro.ui.common.UiStyles;
import com.placepro.ui.common.UiTasks;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

public class UserManagementPanel extends JPanel {

    private final UserManagementService userManagementService;
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel studentModel = createTableModel(
            "ID", "Name", "Email", "Roll Number", "Active");
    private final DefaultTableModel officerModel = createTableModel(
            "ID", "Name", "Email", "Role", "Active");
    private final DefaultTableModel recruiterModel = createTableModel(
            "ID", "Name", "Email", "Company ID", "Active");

    private final JTable studentTable = new JTable(studentModel);
    private final JTable officerTable = new JTable(officerModel);
    private final JTable recruiterTable = new JTable(recruiterModel);

    public UserManagementPanel(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
        setLayout(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", buildUserTab(studentTable, UserType.STUDENT));
        tabs.addTab("Officers", buildUserTab(officerTable, UserType.OFFICER));
        tabs.addTab("Recruiters", buildUserTab(recruiterTable, UserType.RECRUITER));
        add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshButton = new JButton("Refresh All");
        refreshButton.addActionListener(event -> loadAllUsers());
        footer.add(refreshButton);
        footer.add(statusLabel);
        add(footer, BorderLayout.SOUTH);

        loadAllUsers();
    }

    private JPanel buildUserTab(JTable table, UserType userType) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton deactivateButton = new JButton("Deactivate");
        deactivateButton.addActionListener(event -> deactivateSelected(userType, table));
        JButton reactivateButton = new JButton("Reactivate");
        reactivateButton.addActionListener(event -> reactivateSelected(userType, table));
        JButton resetPasswordButton = new JButton("Reset Password");
        resetPasswordButton.addActionListener(event -> resetPassword(userType, table));

        actions.add(deactivateButton);
        actions.add(reactivateButton);
        actions.add(resetPasswordButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAllUsers() {
        statusLabel.setText("Loading users...");
        UiTasks.run(
                () -> new UserLoadResult(
                        userManagementService.listStudents(),
                        userManagementService.listOfficers(),
                        userManagementService.listRecruiters()),
                result -> {
                    loadStudents(result.students);
                    loadOfficers(result.officers);
                    loadRecruiters(result.recruiters);
                    statusLabel.setText("Users loaded.");
                },
                this::showError);
    }

    private void loadStudents(List<Student> students) {
        studentModel.setRowCount(0);
        for (Student student : students) {
            studentModel.addRow(new Object[]{
                    student.getStudentId(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getRollNumber(),
                    Boolean.TRUE.equals(student.getIsActive()) ? "Yes" : "No"
            });
        }
    }

    private void loadOfficers(List<PlacementOfficer> officers) {
        officerModel.setRowCount(0);
        for (PlacementOfficer officer : officers) {
            officerModel.addRow(new Object[]{
                    officer.getOfficerId(),
                    officer.getFullName(),
                    officer.getEmail(),
                    officer.getRole(),
                    Boolean.TRUE.equals(officer.getIsActive()) ? "Yes" : "No"
            });
        }
    }

    private void loadRecruiters(List<Recruiter> recruiters) {
        recruiterModel.setRowCount(0);
        for (Recruiter recruiter : recruiters) {
            recruiterModel.addRow(new Object[]{
                    recruiter.getRecruiterId(),
                    recruiter.getFullName(),
                    recruiter.getEmail(),
                    recruiter.getCompanyId(),
                    Boolean.TRUE.equals(recruiter.getIsActive()) ? "Yes" : "No"
            });
        }
    }

    private void deactivateSelected(UserType userType, JTable table) {
        Integer userId = getSelectedId(table);
        if (userId == null) {
            statusLabel.setText("Select a user first.");
            return;
        }
        UiTasks.run(
                () -> {
                    userType.deactivate(userManagementService, userId);
                    return true;
                },
                ignored -> {
                    statusLabel.setText("User deactivated.");
                    loadAllUsers();
                },
                this::showError);
    }

    private void reactivateSelected(UserType userType, JTable table) {
        Integer userId = getSelectedId(table);
        if (userId == null) {
            statusLabel.setText("Select a user first.");
            return;
        }
        UiTasks.run(
                () -> {
                    userType.reactivate(userManagementService, userId);
                    return true;
                },
                ignored -> {
                    statusLabel.setText("User reactivated.");
                    loadAllUsers();
                },
                this::showError);
    }

    private void resetPassword(UserType userType, JTable table) {
        Integer userId = getSelectedId(table);
        if (userId == null) {
            statusLabel.setText("Select a user first.");
            return;
        }
        UiTasks.run(
                () -> userType.resetPassword(userManagementService, userId),
                temporaryPassword -> JOptionPane.showMessageDialog(
                        this,
                        "Temporary password: " + temporaryPassword,
                        "Password Reset",
                        JOptionPane.INFORMATION_MESSAGE),
                this::showError);
    }

    private Integer getSelectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (Integer) table.getModel().getValueAt(row, 0);
    }

    private DefaultTableModel createTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void showError(Exception exception) {
        Throwable cause = exception.getCause() == null ? exception : exception.getCause();
        statusLabel.setForeground(UiStyles.ERROR_COLOR);
        statusLabel.setText(cause instanceof ServiceException
                ? cause.getMessage()
                : exception.getMessage());
    }

    private enum UserType {
        STUDENT {
            @Override
            void deactivate(UserManagementService service, int id) {
                service.deactivateStudent(id);
            }

            @Override
            void reactivate(UserManagementService service, int id) {
                service.reactivateStudent(id);
            }

            @Override
            String resetPassword(UserManagementService service, int id) {
                return service.resetStudentPassword(id);
            }
        },
        OFFICER {
            @Override
            void deactivate(UserManagementService service, int id) {
                service.deactivateOfficer(id);
            }

            @Override
            void reactivate(UserManagementService service, int id) {
                service.reactivateOfficer(id);
            }

            @Override
            String resetPassword(UserManagementService service, int id) {
                return service.resetOfficerPassword(id);
            }
        },
        RECRUITER {
            @Override
            void deactivate(UserManagementService service, int id) {
                service.deactivateRecruiter(id);
            }

            @Override
            void reactivate(UserManagementService service, int id) {
                service.reactivateRecruiter(id);
            }

            @Override
            String resetPassword(UserManagementService service, int id) {
                return service.resetRecruiterPassword(id);
            }
        };

        abstract void deactivate(UserManagementService service, int id);

        abstract void reactivate(UserManagementService service, int id);

        abstract String resetPassword(UserManagementService service, int id);
    }

    private static final class UserLoadResult {
        private final List<Student> students;
        private final List<PlacementOfficer> officers;
        private final List<Recruiter> recruiters;

        private UserLoadResult(List<Student> students,
                               List<PlacementOfficer> officers,
                               List<Recruiter> recruiters) {
            this.students = students;
            this.officers = officers;
            this.recruiters = recruiters;
        }
    }
}
