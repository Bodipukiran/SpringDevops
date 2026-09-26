package com.dispatchtrack.studentmanagement.service.impl;

import com.dispatchtrack.studentmanagement.dto.StudentDTO;
import com.dispatchtrack.studentmanagement.entity.Student;
import com.dispatchtrack.studentmanagement.exception.DuplicateResourceException;
import com.dispatchtrack.studentmanagement.exception.ResourceNotFoundException;
import com.dispatchtrack.studentmanagement.repository.StudentRepository;
import com.dispatchtrack.studentmanagement.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements the Student business logic on top of {@link StudentRepository}.
 *
 * Also responsible for mapping between the persistence model ({@link Student})
 * and the API model ({@link StudentDTO}), so neither the controller nor the
 * repository needs to know about the other's representation.
 */
@Service
@RequiredArgsConstructor // Lombok: generates a constructor for the final field below (constructor injection)
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final String RESOURCE_NAME = "Student";

    private final StudentRepository studentRepository;

    @Override
    public StudentDTO createStudent(StudentDTO studentDTO) {
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new DuplicateResourceException(
                    "A student with email '" + studentDTO.getEmail() + "' already exists");
        }
        Student student = toEntity(studentDTO);
        Student saved = studentRepository.save(student);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDTO getStudentById(Long id) {
        Student student = findStudentOrThrow(id);
        return toDto(student);
    }

    @Override
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student existing = findStudentOrThrow(id);

        // If the email is being changed, make sure it doesn't collide with another student.
        studentRepository.findByEmail(studentDTO.getEmail())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new DuplicateResourceException(
                            "A student with email '" + studentDTO.getEmail() + "' already exists");
                });

        existing.setName(studentDTO.getName());
        existing.setEmail(studentDTO.getEmail());
        existing.setDepartment(studentDTO.getDepartment());
        existing.setCgpa(studentDTO.getCgpa());

        Student updated = studentRepository.save(existing);
        return toDto(updated);
    }

    @Override
    public void deleteStudent(Long id) {
        Student existing = findStudentOrThrow(id);
        studentRepository.delete(existing);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forId(RESOURCE_NAME, id));
    }

    private Student toEntity(StudentDTO dto) {
        return Student.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .department(dto.getDepartment())
                .cgpa(dto.getCgpa())
                .build();
    }

    private StudentDTO toDto(Student entity) {
        return StudentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .department(entity.getDepartment())
                .cgpa(entity.getCgpa())
                .build();
    }
}
