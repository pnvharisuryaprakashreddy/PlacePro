package com.placepro.dao.impl;

import com.placepro.dao.StudentDAO;
import com.placepro.model.Student;
import com.placepro.util.PasswordUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

class StudentDAOImplTest {

    @Test
    void shouldInsertReadUpdateAndDeleteStudent() {
        StudentDAO studentDAO = new StudentDAOImpl();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        Student student = new Student();
        student.setRollNumber("TEST" + uniqueSuffix);
        student.setFullName("DAO Test Student");
        student.setEmail("dao-test-" + uniqueSuffix + "@placepro.local");
        student.setPhone("9999999999");
        student.setPasswordHash(PasswordUtil.hashPassword("Password@123"));
        student.setBranch("CSE");
        student.setCgpa(new BigDecimal("8.50"));
        student.setBacklogCount(0);
        student.setGraduationYear(2027);
        student.setIsActive(true);

        Student insertedStudent = studentDAO.insert(student);
        Assertions.assertNotNull(insertedStudent.getStudentId());

        Optional<Student> fetchedStudent = studentDAO.findById(insertedStudent.getStudentId());
        Assertions.assertTrue(fetchedStudent.isPresent());
        Assertions.assertEquals(insertedStudent.getEmail(), fetchedStudent.get().getEmail());

        insertedStudent.setFullName("DAO Test Student Updated");
        insertedStudent.setCgpa(new BigDecimal("8.75"));
        boolean updated = studentDAO.update(insertedStudent);
        Assertions.assertTrue(updated);

        Optional<Student> updatedStudent = studentDAO.findByEmail(insertedStudent.getEmail());
        Assertions.assertTrue(updatedStudent.isPresent());
        Assertions.assertEquals("DAO Test Student Updated", updatedStudent.get().getFullName());
        Assertions.assertEquals(new BigDecimal("8.75"), updatedStudent.get().getCgpa());

        boolean deleted = studentDAO.deleteById(insertedStudent.getStudentId());
        Assertions.assertTrue(deleted);
        Assertions.assertFalse(studentDAO.findById(insertedStudent.getStudentId()).isPresent());
    }
}
