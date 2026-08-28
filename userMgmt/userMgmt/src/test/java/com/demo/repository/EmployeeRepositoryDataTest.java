package com.demo.repository;

import com.demo.model.Employee;
import com.demo.model.EmploymentStatus;
import com.demo.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Database-level validation: constraints, persistence and query behaviour.
 * Runs against an isolated in-memory database per test class.
 */
@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryDataTest {

    @Autowired
    private EmployeeRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private Employee employee(String empId, String email, EmploymentStatus status) {
        Employee e = new Employee();
        e.setEmployeeId(empId);
        e.setFullName("Test User " + empId);
        e.setEmail(email);
        e.setPassword("password123");
        e.setDepartment("Engineering");
        e.setDesignation("Engineer");
        e.setRole(Role.EMPLOYEE);
        e.setJoiningDate(LocalDate.now());
        e.setEmploymentStatus(status);
        return e;
    }

    @Test
    void persistsEmployee_withTimestamps() {
        Employee saved = repository.save(employee("E-1", "e1@t.local", EmploymentStatus.ACTIVE));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void employeeIdIsUnique() {
        repository.save(employee("E-1", "a@t.local", EmploymentStatus.ACTIVE));
        assertThatThrownBy(() -> repository.save(employee("E-1", "b@t.local", EmploymentStatus.ACTIVE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emailIsUnique() {
        repository.save(employee("E-1", "same@t.local", EmploymentStatus.ACTIVE));
        assertThatThrownBy(() -> repository.save(employee("E-2", "same@t.local", EmploymentStatus.ACTIVE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByEmailAndEmployeeId_areCaseExactLookups() {
        repository.save(employee("E-9", "find@T.local", EmploymentStatus.ACTIVE));

        assertThat(repository.findByEmail("find@T.local")).isPresent();
        assertThat(repository.findByEmail("find@t.local")).isEmpty();
        assertThat(repository.findByEmployeeId("E-9")).isPresent();
    }

    @Test
    void statusQueries_returnOnlyMatchingRows() {
        repository.save(employee("A-1", "a@t.local", EmploymentStatus.ACTIVE));
        repository.save(employee("I-1", "i@t.local", EmploymentStatus.INACTIVE));
        repository.save(employee("L-1", "l@t.local", EmploymentStatus.ON_LEAVE));
        repository.save(employee("T-1", "t@t.local", EmploymentStatus.TERMINATED));

        assertThat(repository.findByEmploymentStatus(EmploymentStatus.ACTIVE)).hasSize(1);
        assertThat(repository.countByEmploymentStatus(EmploymentStatus.TERMINATED)).isEqualTo(1);
        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    void departmentDistribution_groupsAndCounts() {
        Employee e2 = employee("E-2", "e2@t.local", EmploymentStatus.ACTIVE);
        e2.setDepartment("HR");
        repository.save(employee("D-1", "d1@t.local", EmploymentStatus.ACTIVE));
        repository.save(e2);

        List<Object[]> dist = repository.countByDepartment();
        assertThat(dist).extracting(r -> (String) r[0])
                .containsExactlyInAnyOrder("Engineering", "HR");
    }
}