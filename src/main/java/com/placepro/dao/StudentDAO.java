package com.placepro.dao;

import com.placepro.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentDAO {

    Student insert(Student student);

    Optional<Student> findById(int studentId);

    Optional<Student> findByEmail(String email);

    Optional<Student> findByRollNumber(String rollNumber);

    List<Student> findAllActive();

    List<Student> searchByNameOrRollNumber(String keyword);

    boolean update(Student student);

    boolean deactivate(int studentId);

    boolean deleteById(int studentId);
}
