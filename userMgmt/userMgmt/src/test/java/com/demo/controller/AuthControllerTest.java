package com.demo.controller;

import com.demo.dto.AuthRequest;
import com.demo.dto.AuthResponse;
import com.demo.model.Employee;
import com.demo.model.Role;
import com.demo.repository.EmployeeRepository;
import com.demo.security.JwtUtil;
import com.demo.security.UserDetailsImpl;
import com.demo.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthController authController;

    private Employee testEmployee;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setEmployeeId("EMP-001");
        testEmployee.setFullName("John Doe");
        testEmployee.setEmail("john.doe@example.com");
        testEmployee.setPassword("encodedPassword");
        testEmployee.setRole(Role.EMPLOYEE);

        authRequest = new AuthRequest();
        authRequest.setEmail("john.doe@example.com");
        authRequest.setPassword("password123");
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsValid() {
        UserDetailsImpl userDetails = new UserDetailsImpl(testEmployee, "encodedPassword");
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-jwt-token");

        ResponseEntity<AuthResponse> response = authController.login(authRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().getToken());
        assertEquals(1L, response.getBody().getId());
        assertEquals("EMP-001", response.getBody().getEmployeeId());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
        assertEquals("John Doe", response.getBody().getFullName());
        assertEquals("EMPLOYEE", response.getBody().getRole());
    }

    @Test
    void register_ShouldCreateEmployeeAndReturnAuthResponse() {
        Employee newEmployee = new Employee();
        newEmployee.setEmployeeId("EMP-002");
        newEmployee.setFullName("Jane Doe");
        newEmployee.setEmail("jane.doe@example.com");
        newEmployee.setPassword("password123");
        newEmployee.setRole(Role.EMPLOYEE);
        newEmployee.setDepartment("Engineering");
        newEmployee.setDesignation("Software Engineer");
        newEmployee.setJoiningDate(java.time.LocalDate.now());

        when(employeeRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmployeeId("EMP-002")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-jwt-token");

        ResponseEntity<AuthResponse> response = authController.register(newEmployee);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().getToken());
        verify(employeeRepository).save(any(Employee.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailExists() {
        Employee newEmployee = new Employee();
        newEmployee.setEmail("john.doe@example.com");

        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testEmployee));

        ResponseEntity<AuthResponse> response = authController.register(newEmployee);

        assertEquals(400, response.getStatusCodeValue());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmployeeIdExists() {
        Employee newEmployee = new Employee();
        newEmployee.setEmail("jane.doe@example.com");
        newEmployee.setEmployeeId("EMP-001");

        when(employeeRepository.findByEmail("jane.doe@example.com")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmployeeId("EMP-001")).thenReturn(Optional.of(testEmployee));

        ResponseEntity<AuthResponse> response = authController.register(newEmployee);

        assertEquals(400, response.getStatusCodeValue());
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}