package com.placepro.dao;

import com.placepro.model.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyDAO {

    Company insert(Company company);

    Optional<Company> findById(int companyId);

    Optional<Company> findByCompanyName(String companyName);

    List<Company> findAllActive();

    List<Company> findAll();

    List<Company> searchByName(String keyword);

    /**
     * Server-side company search. Blank/null filters are ignored.
     *
     * @param activeFilter "ACTIVE", "INACTIVE", or null/"ALL"
     * @param driveFilter  "WITH_ACTIVE_DRIVES", "WITHOUT_ACTIVE_DRIVES", or null/"ALL"
     */
    List<Company> searchCompanies(String nameKeyword, String industryKeyword, String activeFilter, String driveFilter);

    boolean update(Company company);

    boolean deactivate(int companyId);

    boolean deleteById(int companyId);
}
