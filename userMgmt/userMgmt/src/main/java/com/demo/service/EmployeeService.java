package com.demo.service;

import com.demo.model.AuditAction;
import com.demo.model.Employee;
import com.demo.model.EmploymentStatus;
import com.demo.model.Role;
import com.demo.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    public Employee createEmployee(Employee employee, Long actorId, String actorEmail, String ipAddress) {
        if (employeeRepository.findByEmployeeId(employee.getEmployeeId()).isPresent()) {
            throw new IllegalArgumentException("Employee ID already exists: " + employee.getEmployeeId());
        }
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + employee.getEmail());
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setCreatedAt(LocalDateTime.now());
        Employee created = employeeRepository.save(employee);

        auditLogService.logAction(actorId, actorEmail, AuditAction.CREATE_EMPLOYEE,
                "Employee", created.getId(),
                "Created employee: " + created.getFullName() + " (" + created.getEmployeeId() + ")",
                ipAddress);

        return created;
    }

    public Employee createEmployee(Employee employee) {
        return createEmployee(employee, null, "SYSTEM", null);
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    public Page<Employee> getAllEmployees(String search, String department, Role role,
                                           EmploymentStatus status, Pageable pageable) {
        return employeeRepository.findWithFilters(search, department, role, status, pageable);
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee, Long actorId, String actorEmail, String ipAddress, String actorRole) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));

        if (!existing.getEmployeeId().equals(updatedEmployee.getEmployeeId()) &&
            employeeRepository.findByEmployeeId(updatedEmployee.getEmployeeId()).isPresent()) {
            throw new IllegalArgumentException("Employee ID already exists: " + updatedEmployee.getEmployeeId());
        }

        if (!existing.getEmail().equals(updatedEmployee.getEmail()) &&
            employeeRepository.findByEmail(updatedEmployee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + updatedEmployee.getEmail());
        }

        String oldRole = existing.getRole().name();
        String newRole = updatedEmployee.getRole().name();

        if (!oldRole.equals(newRole) && actorRole != null && !"ADMIN".equals(actorRole)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only ADMIN can change employee roles");
        }

        existing.setEmployeeId(updatedEmployee.getEmployeeId());
        existing.setFullName(updatedEmployee.getFullName());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setPhone(updatedEmployee.getPhone());
        existing.setDepartment(updatedEmployee.getDepartment());
        existing.setDesignation(updatedEmployee.getDesignation());
        existing.setRole(updatedEmployee.getRole());
        existing.setJoiningDate(updatedEmployee.getJoiningDate());
        existing.setUpdatedAt(LocalDateTime.now());

        Employee saved = employeeRepository.save(existing);

        auditLogService.logAction(actorId, actorEmail, AuditAction.UPDATE_EMPLOYEE,
                "Employee", saved.getId(),
                "Updated employee: " + saved.getFullName() + " (" + saved.getEmployeeId() + ")" +
                        (oldRole.equals(newRole) ? "" : " - Role changed from " + oldRole + " to " + newRole),
                ipAddress);

        return saved;
    }

    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        return updateEmployee(id, updatedEmployee, null, "SYSTEM", null, null);
    }

    public Employee updateEmployeeStatus(Long id, EmploymentStatus status, Long actorId, String actorEmail, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));

        EmploymentStatus oldStatus = employee.getEmploymentStatus();
        validateStatusTransition(oldStatus, status);

        employee.setEmploymentStatus(status);
        employee.setUpdatedAt(LocalDateTime.now());
        Employee saved = employeeRepository.save(employee);

        AuditAction action = switch (status) {
            case ACTIVE -> AuditAction.ACTIVATE_EMPLOYEE;
            case INACTIVE -> AuditAction.DEACTIVATE_EMPLOYEE;
            case ON_LEAVE -> AuditAction.UPDATE_EMPLOYEE;
            case TERMINATED -> AuditAction.TERMINATE_EMPLOYEE;
        };

        auditLogService.logAction(actorId, actorEmail, action,
                "Employee", saved.getId(),
                "Changed status of " + saved.getFullName() + " (" + saved.getEmployeeId() +
                        ") from " + oldStatus + " to " + status,
                ipAddress);

        return saved;
    }

    public Employee updateEmployeeStatus(Long id, EmploymentStatus status) {
        return updateEmployeeStatus(id, status, null, "SYSTEM", null);
    }

    /**
     * Soft delete: marks the employee TERMINATED instead of removing the row,
     * preserving audit history. Terminated employees cannot be reactivated.
     */
    public Employee deleteEmployee(Long id, Long actorId, String actorEmail, String ipAddress) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));

        if (employee.getEmploymentStatus() == EmploymentStatus.TERMINATED) {
            throw new IllegalArgumentException("Employee is already terminated");
        }

        validateStatusTransition(employee.getEmploymentStatus(), EmploymentStatus.TERMINATED);

        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employee.setUpdatedAt(LocalDateTime.now());
        Employee saved = employeeRepository.save(employee);

        auditLogService.logAction(actorId, actorEmail, AuditAction.TERMINATE_EMPLOYEE,
                "Employee", id,
                "Terminated employee: " + saved.getFullName() + " (" + saved.getEmployeeId() + ")",
                ipAddress);

        return saved;
    }

    public Employee deleteEmployee(Long id) {
        return deleteEmployee(id, null, "SYSTEM", null);
    }

    public long getTotalEmployees() {
        return employeeRepository.count();
    }

    public long getActiveEmployees() {
        return employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE);
    }

    public long getInactiveEmployees() {
        return employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE);
    }

    public long getOnLeaveEmployees() {
        return employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE);
    }

    public long getTerminatedEmployees() {
        return employeeRepository.countByEmploymentStatus(EmploymentStatus.TERMINATED);
    }

    public List<Object[]> getDepartmentDistribution() {
        return employeeRepository.countByDepartment();
    }

    public List<Employee> getRecentEmployees(int days, int limit) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        Pageable pageable = Pageable.unpaged();
        return employeeRepository.findRecentEmployees(cutoffDate, pageable).stream().limit(limit).toList();
    }

    private void validateStatusTransition(EmploymentStatus current, EmploymentStatus newStatus) {
        if (current == EmploymentStatus.TERMINATED && newStatus != EmploymentStatus.TERMINATED) {
            throw new IllegalArgumentException("Cannot change status of a terminated employee");
        }
        if (current == EmploymentStatus.TERMINATED && newStatus == EmploymentStatus.TERMINATED) {
            throw new IllegalArgumentException("Employee is already terminated");
        }
    }
}

