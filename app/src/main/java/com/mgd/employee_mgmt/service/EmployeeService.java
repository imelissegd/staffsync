package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.model.Employee;
import com.mgd.employee_mgmt.repository.DepartmentRepository;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository   employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final MessageUtil          msg;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           MessageUtil msg) {
        this.employeeRepository   = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.msg                  = msg;
    }

    public Employee saveEmployee(Employee employee) {
        validateEmployee(employee);

        if (employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException(
                    msg.get("employee.id.duplicate", employee.getEmployeeId()));
        }

        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(Long id, Employee employee) {
        Employee existing = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("employee.not.found.id", id)));

        validateEmployee(employee);

        if (!existing.getEmployeeId().equals(employee.getEmployeeId()) &&
                employeeRepository.existsByEmployeeId(employee.getEmployeeId())) {
            throw new IllegalArgumentException(
                    msg.get("employee.id.duplicate", employee.getEmployeeId()));
        }

        existing.setEmployeeId(employee.getEmployeeId());
        existing.setName(employee.getName());
        existing.setDateOfBirth(employee.getDateOfBirth());
        existing.setDepartment(employee.getDepartment());
        existing.setSalary(employee.getSalary());

        return employeeRepository.save(existing);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new NoSuchElementException(msg.get("employee.not.found.id", id));
        }
        employeeRepository.deleteById(id);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("employee.not.found.id", id)));
    }

    public Employee getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("employee.not.found.employee.id", employeeId)));
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    public List<Employee> searchEmployeesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(msg.get("employee.search.name.empty"));
        }
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Employee> getEmployeesByDepartment(String departmentName) {
        if (departmentName == null || departmentName.trim().isEmpty()) {
            throw new IllegalArgumentException(msg.get("employee.department.filter.empty"));
        }
        Department dept = departmentRepository.findByName(departmentName)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("department.not.found.name", departmentName)));
        return employeeRepository.findByDepartment(dept);
    }

    public double calculateAverageSalary() {
        Double avg = employeeRepository.calculateAverageSalary();
        return avg != null ? avg : 0.0;
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
            throw new IllegalArgumentException(msg.get("employee.id.required"));

        if (employee.getName() == null || employee.getName().trim().isEmpty())
            throw new IllegalArgumentException(msg.get("employee.name.required"));

        if (employee.getDateOfBirth() == null)
            throw new IllegalArgumentException(msg.get("employee.dob.required"));

        int age = employee.getAge();
        if (age < 18 || age > 100)
            throw new IllegalArgumentException(msg.get("employee.age.invalid"));

        if (employee.getDepartment() == null)
            throw new IllegalArgumentException(msg.get("employee.department.required"));

        if (!departmentRepository.existsById(employee.getDepartment().getId())) {
            throw new IllegalArgumentException(
                    msg.get("employee.department.not.exist", employee.getDepartment().getId()));
        }

        if (employee.getSalary() == null || employee.getSalary() <= 0)
            throw new IllegalArgumentException(msg.get("employee.salary.invalid"));
    }
}
