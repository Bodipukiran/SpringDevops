package com.dispatchtrack.studentmanagement.controller;

import com.dispatchtrack.studentmanagement.dto.StudentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration test: boots the real Spring context (controller ->
 * service -> repository -> H2 database) and drives it through MockMvc,
 * exercising the actual REST endpoints end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndFetchStudent_endToEnd() throws Exception {
        StudentDTO newStudent = StudentDTO.builder()
                .name("Rohit Verma")
                .email("rohit.verma@example.com")
                .department("Mechanical Engineering")
                .cgpa(7.9)
                .build();

        String response = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Rohit Verma")))
                .andReturn().getResponse().getContentAsString();

        StudentDTO created = objectMapper.readValue(response, StudentDTO.class);

        mockMvc.perform(get("/students/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("rohit.verma@example.com")));
    }

    @Test
    void createStudent_withInvalidEmail_returnsBadRequest() throws Exception {
        StudentDTO invalid = StudentDTO.builder()
                .name("Bad Email Student")
                .email("not-an-email")
                .department("Physics")
                .cgpa(6.5)
                .build();

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void getStudentById_whenMissing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/students/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void deleteStudent_whenMissing_returnsNotFound() throws Exception {
        mockMvc.perform(delete("/students/{id}", 999_999))
                .andExpect(status().isNotFound());
    }
}
