package com.dispatchtrack.studentmanagement.controller;

import com.dispatchtrack.studentmanagement.dto.StudentDTO;
import com.dispatchtrack.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller exposing CRUD endpoints for {@link StudentDTO}.
 *
 * This layer is intentionally "thin": it only handles HTTP concerns
 * (routing, status codes, request/response bodies) and delegates all
 * business logic to {@link StudentService}. Validation errors and
 * "not found" errors are handled globally by
 * {@link com.dispatchtrack.studentmanagement.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * POST /students
     * Creates a new student. Returns 201 Created with the created resource.
     */
    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO created = studentService.createStudent(studentDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * GET /students
     * Returns the full list of students. Returns 200 OK (even when empty).
     */
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    /**
     * GET /students/{id}
     * Returns a single student. Returns 200 OK, or 404 Not Found via
     * ResourceNotFoundException if no student exists with that id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    /**
     * PUT /students/{id}
     * Fully replaces an existing student's fields. Returns 200 OK with the
     * updated resource, or 404 Not Found if the student does not exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable Long id, @Valid @RequestBody StudentDTO studentDTO) {
        StudentDTO updated = studentService.updateStudent(id, studentDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /students/{id}
     * Deletes a student. Returns 204 No Content on success, or 404 Not Found
     * if the student does not exist.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
