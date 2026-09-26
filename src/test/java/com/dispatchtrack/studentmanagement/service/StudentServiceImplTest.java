package com.dispatchtrack.studentmanagement.service;

import com.dispatchtrack.studentmanagement.dto.StudentDTO;
import com.dispatchtrack.studentmanagement.entity.Student;
import com.dispatchtrack.studentmanagement.exception.ResourceNotFoundException;
import com.dispatchtrack.studentmanagement.repository.StudentRepository;
import com.dispatchtrack.studentmanagement.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link StudentServiceImpl}, with the repository
 * mocked out via Mockito - no Spring context and no database involved,
 * so these run fast and only verify the service's own business logic.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private StudentDTO studentDTO;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .name("Asha Rao")
                .email("asha.rao@example.com")
                .department("Computer Science")
                .cgpa(8.7)
                .build();

        studentDTO = StudentDTO.builder()
                .name("Asha Rao")
                .email("asha.rao@example.com")
                .department("Computer Science")
                .cgpa(8.7)
                .build();
    }

    @Test
    void createStudent_savesAndReturnsDto() {
        when(studentRepository.existsByEmail(studentDTO.getEmail())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentDTO result = studentService.createStudent(studentDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("asha.rao@example.com");
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void getStudentById_whenFound_returnsDto() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentDTO result = studentService.getStudentById(1L);

        assertThat(result.getName()).isEqualTo("Asha Rao");
    }

    @Test
    void getStudentById_whenNotFound_throwsResourceNotFoundException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllStudents_returnsMappedList() {
        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<StudentDTO> result = studentService.getAllStudents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo(student.getEmail());
    }

    @Test
    void deleteStudent_whenNotFound_throwsResourceNotFoundException() {
        when(studentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudent(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStudent_whenFound_updatesFieldsAndSaves() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail(anyString())).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        StudentDTO update = StudentDTO.builder()
                .name("Asha R.")
                .email("asha.rao@example.com")
                .department("Data Science")
                .cgpa(9.1)
                .build();

        StudentDTO result = studentService.updateStudent(1L, update);

        assertThat(result).isNotNull();
        verify(studentRepository, times(1)).save(any(Student.class));
    }
}
