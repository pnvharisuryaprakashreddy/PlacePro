package com.placepro.ui.officer;

import com.placepro.model.Company;
import com.placepro.service.CompanyService;
import com.placepro.service.ServiceException;
import com.placepro.ui.common.UiStyles;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

public class CompanyFormPanel extends JPanel {

    private final CompanyService companyService;
    private final Company existingCompany;
    private final Consumer<Company> onSaved;
    private final JLabel errorLabel = UiStyles.createErrorLabel();

    private final JTextField nameField = new JTextField(24);
    private final JTextField industryField = new JTextField(24);
    private final JTextField contactPersonField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JTextField websiteField = new JTextField(24);
    private final JTextField addressField = new JTextField(24);

    public CompanyFormPanel(CompanyService companyService, Company existingCompany, Consumer<Company> onSaved) {
        this.companyService = companyService;
        this.existingCompany = existingCompany;
        this.onSaved = onSaved;
        buildForm();
        if (existingCompany != null) {
            populate(existingCompany);
        }
    }

    private void buildForm() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 8, 6, 8);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 2;
        constraints.gridx = 0;
        constraints.gridy = 0;

        add(new JLabel(existingCompany == null ? "Add Company" : "Edit Company"), constraints);
        constraints.gridy++;
        add(errorLabel, constraints);

        addField(constraints, "Company Name", nameField);
        addField(constraints, "Industry", industryField);
        addField(constraints, "Contact Person", contactPersonField);
        addField(constraints, "Email", emailField);
        addField(constraints, "Phone", phoneField);
        addField(constraints, "Website", websiteField);
        addField(constraints, "Address", addressField);

        constraints.gridy++;
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(event -> saveCompany());
        add(saveButton, constraints);
    }

    private void addField(GridBagConstraints constraints, String label, JTextField field) {
        constraints.gridwidth = 1;
        constraints.gridy++;
        constraints.gridx = 0;
        add(new JLabel(label), constraints);
        constraints.gridx = 1;
        add(field, constraints);
    }

    private void populate(Company company) {
        nameField.setText(company.getCompanyName());
        industryField.setText(company.getIndustry());
        contactPersonField.setText(company.getContactPerson());
        emailField.setText(company.getEmail());
        phoneField.setText(company.getPhone());
        websiteField.setText(company.getWebsite());
        addressField.setText(company.getAddress());
    }

    private void saveCompany() {
        errorLabel.setText(" ");
        try {
            Company company = existingCompany == null ? new Company() : existingCompany;
            company.setCompanyName(nameField.getText().trim());
            company.setIndustry(industryField.getText().trim());
            company.setContactPerson(contactPersonField.getText().trim());
            company.setEmail(emailField.getText().trim());
            company.setPhone(phoneField.getText().trim());
            company.setWebsite(websiteField.getText().trim());
            company.setAddress(addressField.getText().trim());
            if (company.getIsActive() == null) {
                company.setIsActive(true);
            }

            Company saved = existingCompany == null
                    ? companyService.createCompany(company)
                    : companyService.updateCompany(company);
            onSaved.accept(saved);
        } catch (ServiceException exception) {
            errorLabel.setText(exception.getMessage());
        }
    }
}
