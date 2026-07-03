package com.placepro.service;

import com.placepro.dao.CompanyDAO;
import com.placepro.model.Company;
import com.placepro.service.auth.SessionManager;

import java.util.List;

public class CompanyService {

    private final CompanyDAO companyDAO;
    private final SessionManager sessionManager;

    public CompanyService(CompanyDAO companyDAO, SessionManager sessionManager) {
        this.companyDAO = companyDAO;
        this.sessionManager = sessionManager;
    }

    public Company createCompany(Company company) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (company.getIsActive() == null) {
            company.setIsActive(true);
        }
        return companyDAO.insert(company);
    }

    public Company updateCompany(Company company) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        if (!companyDAO.findById(company.getCompanyId()).isPresent()) {
            throw new ServiceException("Company not found.");
        }
        companyDAO.update(company);
        return company;
    }

    public boolean deactivateCompany(int companyId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return companyDAO.deactivate(companyId);
    }

    public List<Company> listActiveCompanies() {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return companyDAO.findAllActive();
    }

    public List<Company> searchCompanies(String nameKeyword, String industryKeyword, String activeFilter) {
        return searchCompanies(nameKeyword, industryKeyword, activeFilter, null);
    }

    public List<Company> searchCompanies(String nameKeyword,
                                         String industryKeyword,
                                         String activeFilter,
                                         String driveFilter) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        // Filtering happens in SQL (parameterized WHERE clauses), not client-side.
        return companyDAO.searchCompanies(nameKeyword, industryKeyword, activeFilter, driveFilter);
    }

    public Company getCompany(int companyId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return companyDAO.findById(companyId)
                .orElseThrow(() -> new ServiceException("Company not found."));
    }

}
