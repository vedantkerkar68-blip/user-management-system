package com.demo.repository;

import com.demo.model.Employee;
import com.demo.model.EmploymentStatus;
import com.demo.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByEmploymentStatus(EmploymentStatus employmentStatus);

    List<Employee> findByDepartment(String department);

    List<Employee> findByRole(Role role);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:search IS NULL OR LOWER(e.employeeId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:role IS NULL OR e.role = :role) AND " +
           "(:status IS NULL OR e.employmentStatus = :status)")
    Page<Employee> findWithFilters(
            @Param("search") String search,
            @Param("department") String department,
            @Param("role") Role role,
            @Param("status") EmploymentStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.employmentStatus = :status")
    long countByEmploymentStatus(@Param("status") EmploymentStatus status);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.role = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT e.department, COUNT(e) FROM Employee e GROUP BY e.department")
    List<Object[]> countByDepartment();

    @Query("SELECT e FROM Employee e WHERE e.joiningDate >= :date ORDER BY e.joiningDate DESC")
    List<Employee> findRecentEmployees(@Param("date") LocalDate date, Pageable pageable);
}