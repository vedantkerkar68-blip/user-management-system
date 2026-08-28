package com.demo.controller;

import com.demo.model.Employee;
import com.demo.model.EmploymentStatus;
import com.demo.model.Role;
import com.demo.security.UserDetailsImpl;
import com.demo.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    private UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    private String getClientIp() {
        // In a real application, you would extract this from the request
        return "127.0.0.1";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        UserDetailsImpl currentUser = getCurrentUser();
        Long actorId = currentUser != null ? currentUser.getId() : null;
        String actorEmail = currentUser != null ? currentUser.getEmail() : "SYSTEM";
        String ipAddress = getClientIp();

        Employee created = employeeService.createEmployee(employee, actorId, actorEmail, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.EMPLOYEE
                && !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Employees can only view their own profile");
        }
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employee> employees = employeeService.getAllEmployees(search, department, role, status, pageable);
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) {
        UserDetailsImpl currentUser = getCurrentUser();
        Long actorId = currentUser != null ? currentUser.getId() : null;
        String actorEmail = currentUser != null ? currentUser.getEmail() : "SYSTEM";
        String ipAddress = getClientIp();

        Employee updated = employeeService.updateEmployee(id, employee, actorId, actorEmail, ipAddress,
                currentUser != null ? currentUser.getRole().name() : null);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Employee> updateEmployeeStatus(@PathVariable Long id,
                                                          @RequestParam EmploymentStatus status) {
        UserDetailsImpl currentUser = getCurrentUser();
        Long actorId = currentUser != null ? currentUser.getId() : null;
        String actorEmail = currentUser != null ? currentUser.getEmail() : "SYSTEM";
        String ipAddress = getClientIp();

        Employee updated = employeeService.updateEmployeeStatus(id, status, actorId, actorEmail, ipAddress);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Employee> deleteEmployee(@PathVariable Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        Long actorId = currentUser != null ? currentUser.getId() : null;
        String actorEmail = currentUser != null ? currentUser.getEmail() : "SYSTEM";
        String ipAddress = getClientIp();

        Employee terminated = employeeService.deleteEmployee(id, actorId, actorEmail, ipAddress);
        return ResponseEntity.ok(terminated);
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeService.getTotalEmployees());
        stats.put("activeEmployees", employeeService.getActiveEmployees());
        stats.put("inactiveEmployees", employeeService.getInactiveEmployees());
        stats.put("onLeaveEmployees", employeeService.getOnLeaveEmployees());
        stats.put("terminatedEmployees", employeeService.getTerminatedEmployees());
        stats.put("departmentDistribution", employeeService.getDepartmentDistribution());
        stats.put("recentEmployees", employeeService.getRecentEmployees(30, 5));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = employeeService.getDepartmentDistribution().stream()
                .map(row -> (String) row[0])
                .toList();
        return ResponseEntity.ok(departments);
    }
}