package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.service.EmployeeService;
import com.mgd.employee_mgmt.util.MessageUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.employees.base}")
@CrossOrigin(origins = "${cors.allowed.origins}")
@PropertySource("classpath:urls.properties")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final MessageUtil msg;

    @Autowired
    public EmployeeController(EmployeeService employeeService, MessageUtil msg) {
        this.employeeService = employeeService;
        this.msg = msg;
    }

    // POST /api/employees
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) {
        Employee savedEmployee = employeeService.saveEmployee(employee);
        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    // GET /api/employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // GET /api/employees/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // GET /api/employees/employee-id/{employeeId}
    @GetMapping("${api.employees.by.employee.id}")
    public ResponseEntity<Employee> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        return ResponseEntity.ok(employeeService.getEmployeeByEmployeeId(employeeId));
    }

    // PUT /api/employees/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody Employee employee) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employee));
    }

    // DELETE /api/employees/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", msg.get("employee.deleted.success"));
        return ResponseEntity.ok(response);
    }

    // GET /api/employees/search?name=...
    @GetMapping("${api.employees.search}")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam String name) {
        return ResponseEntity.ok(employeeService.searchEmployeesByName(name));
    }

    // GET /api/employees/department/{department}
    @GetMapping("${api.employees.by.department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
    }

    // GET /api/employees/statistics/average-salary
    @GetMapping("${api.employees.avg.salary}")
    public ResponseEntity<Map<String, Double>> getAverageSalary() {
        Map<String, Double> response = new HashMap<>();
        response.put("averageSalary", employeeService.calculateAverageSalary());
        return ResponseEntity.ok(response);
    }

    // GET /api/employees/statistics/average-age
    @GetMapping("${api.employees.avg.age}")
    public ResponseEntity<Map<String, Double>> getAverageAge() {
        Map<String, Double> response = new HashMap<>();
        response.put("averageAge", employeeService.calculateAverageAge());
        return ResponseEntity.ok(response);
    }

    // GET /api/employees/reports/by-department
    @GetMapping("${api.employees.report.by.department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartmentReport() {
        return ResponseEntity.ok(employeeService.getEmployeesOrderedByDepartment());
    }

    // GET /api/employees/reports/by-age
    @GetMapping("${api.employees.report.by.age}")
    public ResponseEntity<List<Employee>> getEmployeesByAgeReport() {
        return ResponseEntity.ok(employeeService.getEmployeesOrderedByAge());
    }
}
