package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.repository.DepartmentRepository;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service class with business logic.
 * Demonstrates use of Collections (ArrayList, List).
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Employee saveEmployee(Employee employee) {
        validateEmployee(employee);

        if (employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException(
                    "Employee with ID " + employee.getEmployeeId() + " already exists");
        }

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Employee not found with id: " + id));

        validateEmployee(employee);

        existing.setName(employee.getName());
        existing.setDateOfBirth(employee.getDateOfBirth());
        existing.setDepartment(employee.getDepartment());
        existing.setSalary(employee.getSalary());

        return employeeRepository.save(existing);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new NoSuchElementException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Employee not found with id: " + id));
    }

    public Employee getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Employee not found with employee ID: " + employeeId));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> searchEmployeesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Returns employees by department name.
     * Looks up the Department entity first, then delegates to the repository.
     */
    public List<Employee> getEmployeesByDepartment(String departmentName) {
        if (departmentName == null || departmentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty");
        }
        Department dept = departmentRepository.findByName(departmentName)
                .orElseThrow(() -> new NoSuchElementException(
                        "Department not found with name: " + departmentName));
        return employeeRepository.findByDepartment(dept);
    }

    public double calculateAverageSalary() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) return 0.0;
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public double calculateAverageAge() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) return 0.0;
        return employees.stream()
                .mapToInt(Employee::getAge)
                .average()
                .orElse(0.0);
    }

    public List<Employee> getEmployeesOrderedByDepartment() {
        return employeeRepository.findAllOrderByDepartment();
    }

    public List<Employee> getEmployeesOrderedByAge() {
        List<Employee> employees = new ArrayList<>(employeeRepository.findAll());
        employees.sort(Comparator.comparingInt(Employee::getAge));
        return employees;
    }

    // ── private validation ───────────────────────────────────────────────────

    private void validateEmployee(Employee employee) {
        if (employee.getEmployeeId() == null || employee.getEmployeeId().trim().isEmpty())
            throw new IllegalArgumentException("Employee ID is required");

        if (employee.getName() == null || employee.getName().trim().isEmpty())
            throw new IllegalArgumentException("Employee name is required");

        if (employee.getDateOfBirth() == null)
            throw new IllegalArgumentException("Date of birth is required");

        int age = employee.getAge();
        if (age < 18 || age > 100)
            throw new IllegalArgumentException("Employee age must be between 18 and 100");

        if (employee.getDepartment() == null)
            throw new IllegalArgumentException("Department is required");

        // Verify the referenced department actually exists in the DB
        if (!departmentRepository.existsById(employee.getDepartment().getId())) {
            throw new IllegalArgumentException(
                    "Department with id " + employee.getDepartment().getId() + " does not exist");
        }

        if (employee.getSalary() == null || employee.getSalary() <= 0)
            throw new IllegalArgumentException("Salary must be greater than 0");
    }
}