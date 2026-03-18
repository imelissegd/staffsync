package com.mgd.employee_mgmt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees")
public class Employee extends Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Employee ID is required")
    @Column(unique = true, nullable = false, length = 50)
    private String employeeId;

    /**
     * ManyToOne relationship: many employees belong to one department.
     * department_id FK is stored in the employees table.
     * LAZY fetch to avoid N+1; JSON serialization ignores hibernate proxy fields.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.01", message = "Salary must be greater than 0")
    @Column(nullable = false)
    private Double salary;

    // Constructors
    public Employee() {
        super();
    }

    public Employee(String employeeId, String name, LocalDate dateOfBirth,
                    Department department, Double salary) {
        super(name, dateOfBirth);
        this.employeeId = employeeId;
        this.department = department;
        this.salary = salary;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    // Polymorphism - Overriding abstract method from Person
    @Override
    public String getDetails() {
        String deptName = department != null ? department.getName() : "N/A";
        return String.format("Employee[ID=%s, Name=%s, Age=%d, Department=%s, Salary=%.2f]",
                employeeId, getName(), getAge(), deptName, salary);
    }

    public boolean isValidSalary() {
        return salary != null && salary > 0;
    }

    @Override
    public String toString() {
        return getDetails();
    }
}