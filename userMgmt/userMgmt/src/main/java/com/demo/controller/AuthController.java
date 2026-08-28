package com.demo.controller;

import com.demo.dto.AuthRequest;
import com.demo.dto.AuthResponse;
import com.demo.model.AuditAction;
import com.demo.model.Employee;
import com.demo.model.Role;
import com.demo.repository.EmployeeRepository;
import com.demo.security.JwtUtil;
import com.demo.security.UserDetailsImpl;
import com.demo.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        String ipAddress = getClientIp();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtUtil.generateToken(userDetails.getUsername(), "ROLE_" + userDetails.getRole().name());

            auditLogService.logAction(userDetails.getId(), userDetails.getEmail(), AuditAction.LOGIN,
                    "User", userDetails.getId(),
                    "User logged in successfully",
                    ipAddress);

            return ResponseEntity.ok(new AuthResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getEmployeeId(),
                    userDetails.getEmail(),
                    userDetails.getFullName(),
                    userDetails.getRole().name()
            ));
        } catch (BadCredentialsException e) {
            auditLogService.logAction(null, request.getEmail(), AuditAction.FAILED_LOGIN,
                    "User", null,
                    "Failed login attempt for email: " + request.getEmail(),
                    ipAddress);
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody Employee employee) {
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        if (employeeRepository.findByEmployeeId(employee.getEmployeeId()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setRole(employee.getRole() != null ? employee.getRole() : Role.EMPLOYEE);
        Employee saved = employeeRepository.save(employee);

        auditLogService.logAction(saved.getId(), saved.getEmail(), AuditAction.CREATE_EMPLOYEE,
                "Employee", saved.getId(),
                "User registered: " + saved.getFullName() + " (" + saved.getEmployeeId() + ")",
                getClientIp());

        String jwt = jwtUtil.generateToken(saved.getEmail(), "ROLE_" + saved.getRole().name());

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                saved.getId(),
                saved.getEmployeeId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole().name()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(
                null,
                userDetails.getId(),
                userDetails.getEmployeeId(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                userDetails.getRole().name()
        ));
    }

    private String getClientIp() {
        return "127.0.0.1";
    }
}