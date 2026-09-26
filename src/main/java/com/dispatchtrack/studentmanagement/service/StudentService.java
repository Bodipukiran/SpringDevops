package com.dispatchtrack.studentmanagement.service;

import com.dispatchtrack.studentmanagement.dto.StudentDTO;

import java.util.List;

/**
 * Service-layer contract for Student business logic.
 *
 * The controller depends on this interface rather than the implementation
 * class directly, which keeps the layers loosely coupled and makes the
 * service easy to mock in controller unit tests.
 */
public interface StudentService {

    StudentDTO createStudent(StudentDTO studentDTO);

    List<StudentDTO> getAllStudents();

    StudentDTO getStudentById(Long id);

    StudentDTO updateStudent(Long id, StudentDTO studentDTO);

    void deleteStudent(Long id);
}
