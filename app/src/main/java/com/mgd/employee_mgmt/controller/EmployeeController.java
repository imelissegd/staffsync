package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Create new employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee savedEmployee = employeeService.saveEmployee(employee);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    // Get employee by Employee ID
    @GetMapping("/employee-id/{employeeId}")
    public ResponseEntity<Employee> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        Employee employee = employeeService.getEmployeeByEmployeeId(employeeId);
        return ResponseEntity.ok(employee);
    }

    // Update employee
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id, 
            @Valid @RequestBody Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    // Delete employee
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Employee deleted successfully");
        return ResponseEntity.ok(response);
    }

    // Search employees by name
    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam String name) {
        List<Employee> employees = employeeService.searchEmployeesByName(name);
        return ResponseEntity.ok(employees);
    }

    // Get employees by department
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable String department) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(employees);
    }

    // Calculate average salary
    @GetMapping("/statistics/average-salary")
    public ResponseEntity<Map<String, Double>> getAverageSalary() {
        double avgSalary = employeeService.calculateAverageSalary();
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", avgSalary);
        return ResponseEntity.ok(response);
    }

    // Calculate average age
    @GetMapping("/statistics/average-age")
    public ResponseEntity<Map<String, Double>> getAverageAge() {
        double avgAge = employeeService.calculateAverageAge();
        Map<String, Double> response = new HashMap<>();
        response.put("averageAge", avgAge);
        return ResponseEntity.ok(response);
    }

    // Get employees ordered by department
    @GetMapping("/reports/by-department")
    public ResponseEntity<List<Employee>> getEmployeesByDepartmentReport() {
        List<Employee> employees = employeeService.getEmployeesOrderedByDepartment();
        return ResponseEntity.ok(employees);
    }

    // Get employees ordered by age
    @GetMapping("/reports/by-age")
    public ResponseEntity<List<Employee>> getEmployeesByAgeReport() {
        List<Employee> employees = employeeService.getEmployeesOrderedByAge();
        return ResponseEntity.ok(employees);
    }
}