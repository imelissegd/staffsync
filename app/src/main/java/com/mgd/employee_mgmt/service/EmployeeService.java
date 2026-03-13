package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service class with business logic
 * Demonstrates use of Collections (ArrayList, List)
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee saveEmployee(Employee employee) {
        // Validation
        validateEmployee(employee);
        
        // Check for duplicate employee ID
        if (employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {
            throw new RuntimeException("Employee with ID " + employee.getEmployeeId() + " already exists");
        }
        
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employee) {
        // Check if employee exists
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Validate new data
        validateEmployee(employee);
        
        // Update fields
        existingEmployee.setName(employee.getName());
        existingEmployee.setDateOfBirth(employee.getDateOfBirth());
        existingEmployee.setDepartment(employee.getDepartment());
        existingEmployee.setSalary(employee.getSalary());
        
        return employeeRepository.save(existingEmployee);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with employee ID: " + employeeId));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> searchEmployeesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Search name cannot be empty");
        }
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new RuntimeException("Department cannot be empty");
        }
        return employeeRepository.findByDepartment(department);
    }

    public double calculateAverageSalary() {
        // Using Collection interface (List) and Java Streams
        List<Employee> employees = employeeRepository.findAll();
        
        if (employees.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average using streams
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public double calculateAverageAge() {
        // Using Collection interface and Polymorphism (getAge() from Person class)
        List<Employee> employees = employeeRepository.findAll();
        
        if (employees.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average age using streams
        return employees.stream()
                .mapToInt(Employee::getAge)  // Using inherited method from Person
                .average()
                .orElse(0.0);
    }

    public List<Employee> getEmployeesOrderedByDepartment() {
        return employeeRepository.findAllOrderByDepartment();
    }

    public List<Employee> getEmployeesOrderedByAge() {
        // Using Collection interface with custom sorting
        List<Employee> employees = new ArrayList<>(employeeRepository.findAll());
        
        // Sort by age using Comparator
        employees.sort(Comparator.comparingInt(Employee::getAge));
        
        return employees;
    }

    // Private validation method
    private void validateEmployee(Employee employee) {
        // Validation for null values
        if (employee.getEmployeeId() == null || employee.getEmployeeId().trim().isEmpty()) {
            throw new RuntimeException("Employee ID is required");
        }
        
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new RuntimeException("Employee name is required");
        }
        
        if (employee.getDateOfBirth() == null) {
            throw new RuntimeException("Date of birth is required");
        }
        
        if (employee.getDepartment() == null || employee.getDepartment().trim().isEmpty()) {
            throw new RuntimeException("Department is required");
        }
        
        if (employee.getSalary() == null || employee.getSalary() <= 0) {
            throw new RuntimeException("Salary must be greater than 0");
        }
        

    }
}