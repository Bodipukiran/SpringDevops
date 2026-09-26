package com.dispatchtrack.studentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Spring Boot auto-configures the embedded Tomcat
 * server, Spring MVC dispatcher, JPA/Hibernate, Actuator endpoints, etc.
 * based on the starters present on the classpath (see pom.xml) and the
 * settings in application.properties.
 */
@SpringBootApplication
public class StudentManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementSystemApplication.class, args);
    }
}
