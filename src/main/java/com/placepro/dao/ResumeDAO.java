package com.placepro.dao;

import com.placepro.model.Resume;

import java.util.List;
import java.util.Optional;

public interface ResumeDAO {

    Resume insert(Resume resume);

    Optional<Resume> findById(int resumeId);

    Optional<Resume> findCurrentByStudentId(int studentId);

    List<Resume> findByStudentId(int studentId);

    boolean update(Resume resume);

    boolean deleteById(int resumeId);
}
