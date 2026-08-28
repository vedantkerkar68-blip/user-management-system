package com.demo.security;

import com.demo.model.Employee;
import com.demo.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String employeeId;
    private final String email;
    private final String fullName;
    private final String password;
    private final Role role;
    private final boolean enabled;

    public UserDetailsImpl(Employee employee, String password) {
        this.id = employee.getId();
        this.employeeId = employee.getEmployeeId();
        this.email = employee.getEmail();
        this.fullName = employee.getFullName();
        this.password = password;
        this.role = employee.getRole();
        this.enabled = employee.getEmploymentStatus() == com.demo.model.EmploymentStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}