package com.placepro.dao;

import com.placepro.model.Recruiter;

import java.util.List;
import java.util.Optional;

public interface RecruiterDAO {

    Recruiter insert(Recruiter recruiter);

    Optional<Recruiter> findById(int recruiterId);

    Optional<Recruiter> findByEmail(String email);

    List<Recruiter> findByCompanyId(int companyId);

    List<Recruiter> findAllActive();

    List<Recruiter> findAll();

    boolean update(Recruiter recruiter);

    boolean deactivate(int recruiterId);

    boolean deleteById(int recruiterId);
}
