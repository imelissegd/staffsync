package com.mgd.employee_mgmt.repository;

import com.mgd.employee_mgmt.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // Find employee by employeeId
    Optional<Employee> findByEmployeeId(String employeeId);
    
    // Find all employees by department
    List<Employee> findByDepartment(String department);
    
    // Check if employee exists by employeeId
    boolean existsByEmployeeId(String employeeId);
    
    // Custom query - Calculate average salary
    @Query("SELECT AVG(e.salary) FROM Employee e")
    Double calculateAverageSalary();
    
    // Custom query - Find all employees ordered by department
    @Query("SELECT e FROM Employee e ORDER BY e.department, e.name")
    List<Employee> findAllOrderByDepartment();
    
    // Search employees by name (case-insensitive)
    List<Employee> findByNameContainingIgnoreCase(String name);
    
    // Find employees with salary greater than specified amount
    List<Employee> findBySalaryGreaterThanEqual(Double salary);
}