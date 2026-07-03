package com.placepro.service;

import com.placepro.dao.CompanyDAO;
import com.placepro.model.Company;
import com.placepro.service.auth.SessionManager;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);

        String normalizedName = nameKeyword == null ? "" : nameKeyword.trim().toLowerCase(Locale.ENGLISH);
        String normalizedIndustry = industryKeyword == null ? "" : industryKeyword.trim().toLowerCase(Locale.ENGLISH);

        return companyDAO.findAll().stream()
                .filter(company -> matchesActiveFilter(company, activeFilter))
                .filter(company -> normalizedName.isEmpty()
                        || company.getCompanyName().toLowerCase(Locale.ENGLISH).contains(normalizedName))
                .filter(company -> normalizedIndustry.isEmpty()
                        || (company.getIndustry() != null
                        && company.getIndustry().toLowerCase(Locale.ENGLISH).contains(normalizedIndustry)))
                .collect(Collectors.toList());
    }

    public Company getCompany(int companyId) {
        AuthorizationHelper.requireRole(sessionManager, UserRole.OFFICER, UserRole.ADMIN);
        return companyDAO.findById(companyId)
                .orElseThrow(() -> new ServiceException("Company not found."));
    }

    private boolean matchesActiveFilter(Company company, String activeFilter) {
        if (activeFilter == null || activeFilter.isBlank() || "ALL".equalsIgnoreCase(activeFilter)) {
            return true;
        }
        if ("ACTIVE".equalsIgnoreCase(activeFilter)) {
            return Boolean.TRUE.equals(company.getIsActive());
        }
        if ("INACTIVE".equalsIgnoreCase(activeFilter)) {
            return !Boolean.TRUE.equals(company.getIsActive());
        }
        return true;
    }
}
