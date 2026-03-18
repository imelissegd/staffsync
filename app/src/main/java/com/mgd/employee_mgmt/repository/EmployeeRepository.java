package com.mgd.employee_mgmt.repository;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeId(String employeeId);

    // Find all employees belonging to a specific Department entity
    List<Employee> findByDepartment(Department department);

    // Check whether any employee belongs to a given department (used for delete-block)
    boolean existsByDepartment(Department department);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT AVG(e.salary) FROM Employee e")
    Double calculateAverageSalary();

    @Query("SELECT e FROM Employee e ORDER BY e.department.name, e.name")
    List<Employee> findAllOrderByDepartment();

    List<Employee> findByNameContainingIgnoreCase(String name);

    List<Employee> findBySalaryGreaterThanEqual(Double salary);
}