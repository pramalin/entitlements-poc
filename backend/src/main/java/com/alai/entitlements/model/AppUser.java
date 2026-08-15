package com.alai.entitlements.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "department")
    private String department;

    @Column(name = "title")
    private String title;

    @Column(name = "manager_name")
    private String managerName;

    public AppUser() {
        // JPA (public so test code can build fixtures with ReflectionTestUtils)
    }

    public Integer getUserId() {
        return userId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDepartment() {
        return department;
    }

    public String getTitle() {
        return title;
    }

    public String getManagerName() {
        return managerName;
    }
}
