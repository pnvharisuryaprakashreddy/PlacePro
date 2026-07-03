package com.placepro.dao;

import com.placepro.model.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyDAO {

    Company insert(Company company);

    Optional<Company> findById(int companyId);

    Optional<Company> findByCompanyName(String companyName);

    List<Company> findAllActive();

    List<Company> searchByName(String keyword);

    boolean update(Company company);

    boolean deactivate(int companyId);

    boolean deleteById(int companyId);
}
