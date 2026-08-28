package com.demo.service;

import com.demo.model.Employee;
import com.demo.model.EmploymentStatus;
import com.demo.model.Role;
import com.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setEmployeeId("EMP-001");
        testEmployee.setFullName("John Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setPhone("1234567890");
        testEmployee.setDepartment("Engineering");
        testEmployee.setDesignation("Software Engineer");
        testEmployee.setRole(Role.EMPLOYEE);
        testEmployee.setJoiningDate(LocalDate.now());
        testEmployee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setPassword("encodedPassword");
    }

    @Test
    void createEmployee_ShouldEncodePasswordAndSave() {
        when(employeeRepository.findByEmployeeId(anyString())).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        Employee result = employeeService.createEmployee(testEmployee);

        assertNotNull(result);
        assertEquals("EMP-001", result.getEmployeeId());
        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder).encode(testEmployee.getPassword());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void createEmployee_ShouldThrowException_WhenEmployeeIdExists() {
        when(employeeRepository.findByEmployeeId("EMP-001")).thenReturn(Optional.of(testEmployee));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });

        assertEquals("Employee ID already exists: EMP-001", exception.getMessage());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void createEmployee_ShouldThrowException_WhenEmailExists() {
        when(employeeRepository.findByEmployeeId("EMP-001")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testEmployee));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.createEmployee(testEmployee);
        });

        assertEquals("Email already exists: john.doe@example.com", exception.getMessage());
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee_WhenExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        Optional<Employee> result = employeeService.getEmployeeById(1L);

        assertTrue(result.isPresent());
        assertEquals(testEmployee, result.get());
    }

    @Test
    void getEmployeeById_ShouldReturnEmpty_WhenNotExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateEmployee_ShouldUpdateFields_WhenValid() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmployeeId("EMP-002");
        updatedEmployee.setFullName("Jane Doe");
        updatedEmployee.setEmail("jane.doe@example.com");
        updatedEmployee.setPhone("0987654321");
        updatedEmployee.setDepartment("Marketing");
        updatedEmployee.setDesignation("Marketing Manager");
        updatedEmployee.setRole(Role.MANAGER);
        updatedEmployee.setJoiningDate(LocalDate.now().minusMonths(6));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.findByEmployeeId("EMP-002")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, updatedEmployee);

        assertEquals("EMP-002", result.getEmployeeId());
        assertEquals("Jane Doe", result.getFullName());
        assertEquals("jane.doe@example.com", result.getEmail());
        assertEquals("0987654321", result.getPhone());
        assertEquals("Marketing", result.getDepartment());
        assertEquals("Marketing Manager", result.getDesignation());
        assertEquals(Role.MANAGER, result.getRole());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateEmployeeStatus_ShouldChangeStatus_WhenValid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee result = employeeService.updateEmployeeStatus(1L, EmploymentStatus.ON_LEAVE);

        assertEquals(EmploymentStatus.ON_LEAVE, result.getEmploymentStatus());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateEmployeeStatus_ShouldThrowException_WhenTransitionFromTerminated() {
        testEmployee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.updateEmployeeStatus(1L, EmploymentStatus.ACTIVE);
        });

        assertEquals("Cannot change status of a terminated employee", exception.getMessage());
    }

    @Test
    void deleteEmployee_ShouldTerminateNotRemove_WhenExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.deleteEmployee(1L);

        assertEquals(EmploymentStatus.TERMINATED, result.getEmploymentStatus());
        verify(employeeRepository, never()).deleteById(anyLong());
        verify(auditLogService).logAction(any(), anyString(), any(), anyString(), anyLong(), anyString(), any());
    }

    @Test
    void deleteEmployee_ShouldThrowException_WhenAlreadyTerminated() {
        testEmployee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employeeService.deleteEmployee(1L));

        assertEquals("Employee is already terminated", exception.getMessage());
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateEmployee_ShouldRejectRoleChange_WhenActorIsNotAdmin() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmployeeId("EMP-001");
        updatedEmployee.setFullName("John Doe");
        updatedEmployee.setEmail("john.doe@example.com");
        updatedEmployee.setRole(Role.ADMIN);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                employeeService.updateEmployee(1L, updatedEmployee, 9L, "hr@x.com", "127.0.0.1", "HR"));
    }

    @Test
    void updateEmployee_ShouldAllowRoleChange_WhenActorIsAdmin() {
        Employee updatedEmployee = new Employee();
        updatedEmployee.setEmployeeId("EMP-001");
        updatedEmployee.setFullName("John Doe");
        updatedEmployee.setEmail("john.doe@example.com");
        updatedEmployee.setRole(Role.MANAGER);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, updatedEmployee, 1L, "admin@x.com", "127.0.0.1", "ADMIN");

        assertEquals(Role.MANAGER, result.getRole());
    }

    @Test
    void deleteEmployee_ShouldThrowException_WhenNotExists() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeService.deleteEmployee(1L);
        });

        assertEquals("Employee not found with id: 1", exception.getMessage());
        verify(employeeRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllEmployees_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> page = new PageImpl<>(List.of(testEmployee), pageable, 1);
        when(employeeRepository.findWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        Page<Employee> result = employeeService.getAllEmployees(null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getDashboardStats_ShouldReturnCounts() {
        when(employeeRepository.count()).thenReturn(100L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ACTIVE)).thenReturn(80L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.INACTIVE)).thenReturn(10L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.ON_LEAVE)).thenReturn(5L);
        when(employeeRepository.countByEmploymentStatus(EmploymentStatus.TERMINATED)).thenReturn(5L);
        when(employeeRepository.countByDepartment()).thenReturn(List.of(new Object[]{"Engineering", 50L}, new Object[]{"HR", 10L}));
        when(employeeRepository.findRecentEmployees(any(), any())).thenReturn(List.of(testEmployee));

        assertEquals(100L, employeeService.getTotalEmployees());
        assertEquals(80L, employeeService.getActiveEmployees());
        assertEquals(10L, employeeService.getInactiveEmployees());
        assertEquals(5L, employeeService.getOnLeaveEmployees());
        assertEquals(5L, employeeService.getTerminatedEmployees());
    }
}