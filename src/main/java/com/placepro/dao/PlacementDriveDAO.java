package com.placepro.dao;

import com.placepro.model.PlacementDrive;

import java.util.List;
import java.util.Optional;

public interface PlacementDriveDAO {

    PlacementDrive insert(PlacementDrive drive);

    Optional<PlacementDrive> findById(int driveId);

    List<PlacementDrive> findByCompanyId(int companyId);

    List<PlacementDrive> findByStatus(String status);

    List<PlacementDrive> findPublishedDrives();

    List<PlacementDrive> findAll();

    boolean update(PlacementDrive drive);

    boolean deleteById(int driveId);
}
