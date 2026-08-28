package com.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", uniqueConstraints = {
    @UniqueConstraint(columnNames = "employee_id"),
    @UniqueConstraint(columnNames = "email")
})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 20, message = "Employee ID must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Employee ID must contain only uppercase letters, numbers, and hyphens")
    @Column(name = "employee_id", nullable = false, unique = true, length = 20)
    private String employeeId;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be exactly 10 digits")
    @Column(name = "phone", length = 15)
    private String phone;

    @NotBlank(message = "Department is required")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @NotBlank(message = "Designation is required")
    @Size(min = 2, max = 100, message = "Designation must be between 2 and 100 characters")
    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @NotNull(message = "Joining date is required")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @NotNull(message = "Employment status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    private EmploymentStatus employmentStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false)
    private String password;

    public Employee() {
        this.createdAt = LocalDateTime.now();
        this.employmentStatus = EmploymentStatus.ACTIVE;
    }

    public Employee(String employeeId, String fullName, String email, String phone,
                    String department, String designation, Role role,
                    LocalDate joiningDate, EmploymentStatus employmentStatus) {
        this();
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.designation = designation;
        this.role = role;
        this.joiningDate = joiningDate;
        this.employmentStatus = employmentStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public EmploymentStatus getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Employee [id=" + id + ", employeeId=" + employeeId + ", fullName=" + fullName +
                ", email=" + email + ", phone=" + phone + ", department=" + department +
                ", designation=" + designation + ", role=" + role + ", joiningDate=" + joiningDate +
                ", employmentStatus=" + employmentStatus + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }
}