package com.demo.config;

import com.demo.model.Employee;
import com.demo.model.EmploymentStatus;
import com.demo.model.Role;
import com.demo.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * DEVELOPMENT ONLY - seeds demo accounts so a fresh local clone is usable.
 * Never active in production (profile-gated). Passwords are BCrypt-hashed.
 */
@Component
@Profile("local")
public class DevDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevDataInitializer.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (employeeRepository.count() > 0) {
            return;
        }
        logger.info("Seeding development demo accounts...");
        seed("ADMIN-001", "Admin User", "admin@company.com", "IT", "System Administrator", Role.ADMIN);
        seed("HR-001", "HR User", "hr@company.com", "Human Resources", "HR Specialist", Role.HR);
        seed("MGR-001", "Manager User", "manager@company.com", "Engineering", "Engineering Manager", Role.MANAGER);
        seed("EMP-001", "Employee User", "employee@company.com", "Engineering", "Software Engineer", Role.EMPLOYEE);
        logger.info("Development demo accounts created (all use password 'password123' locally only)");
    }

    private void seed(String employeeId, String name, String email, String department,
                      String designation, Role role) {
        Employee e = new Employee();
        e.setEmployeeId(employeeId);
        e.setFullName(name);
        e.setEmail(email);
        e.setPassword(passwordEncoder.encode("password123"));
        e.setPhone("1234567890");
        e.setDepartment(department);
        e.setDesignation(designation);
        e.setRole(role);
        e.setJoiningDate(LocalDate.of(2024, 1, 1));
        e.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employeeRepository.save(e);
    }
}