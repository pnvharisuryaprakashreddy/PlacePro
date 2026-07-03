package com.placepro.dao;

import com.placepro.model.PlacementOfficer;

import java.util.List;
import java.util.Optional;

public interface PlacementOfficerDAO {

    PlacementOfficer insert(PlacementOfficer officer);

    Optional<PlacementOfficer> findById(int officerId);

    Optional<PlacementOfficer> findByEmail(String email);

    Optional<PlacementOfficer> findByEmployeeId(String employeeId);

    List<PlacementOfficer> findAllActive();

    List<PlacementOfficer> findAll();

    List<PlacementOfficer> findByRole(String role);

    boolean update(PlacementOfficer officer);

    boolean deactivate(int officerId);

    boolean deleteById(int officerId);
}
