package com.dispatchtrack.studentmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object exposed by the REST API.
 *
 * Keeping a DTO separate from the {@link com.dispatchtrack.studentmanagement.entity.Student}
 * entity means the JSON contract clients see is decoupled from the database
 * schema, and lets us attach request-validation rules (Bean Validation) here
 * without polluting the entity used by JPA/Hibernate.
 *
 * "id" is ignored on create requests (the database generates it) and is
 * simply echoed back on responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDTO {

    private Long id;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Department must not be blank")
    private String department;

    @NotNull(message = "CGPA must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "CGPA must be between 0.0 and 10.0")
    @DecimalMax(value = "10.0", inclusive = true, message = "CGPA must be between 0.0 and 10.0")
    private Double cgpa;
}
