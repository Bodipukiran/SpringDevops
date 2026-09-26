package com.dispatchtrack.studentmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity that maps to the "students" table in MySQL.
 *
 * This is the persistence-layer model only. It is never returned directly
 * from the controller - the {@link com.dispatchtrack.studentmanagement.dto.StudentDTO}
 * is used for that, keeping the API contract separate from the database schema.
 */
@Entity
@Table(name = "students")
@Data               // Lombok: generates getters, setters, toString, equals & hashCode
@NoArgsConstructor   // Lombok: required no-arg constructor for JPA/Hibernate
@AllArgsConstructor  // Lombok: convenience constructor with all fields
@Builder             // Lombok: fluent Student.builder()...build() style construction
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(nullable = false)
    private Double cgpa;
}
