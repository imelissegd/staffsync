package com.mgd.employee_mgmt.service;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.repository.DepartmentRepository;
import com.mgd.employee_mgmt.repository.EmployeeRepository;
import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository   employeeRepository;
    private final MessageUtil          msg;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository,
                             EmployeeRepository employeeRepository,
                             MessageUtil msg) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository   = employeeRepository;
        this.msg                  = msg;
    }

    /** Create a new department — name must be unique and non-blank. */
    public Department createDepartment(Department department) {
        validateDepartment(department);

        if (departmentRepository.existsByName(department.getName().trim())) {
            throw new IllegalArgumentException(
                    msg.get("department.name.duplicate", department.getName()));
        }

        department.setName(department.getName().trim());
        return departmentRepository.save(department);
    }

    /** Return all departments ordered by name. */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    /** Return a single department by its primary key. */
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("department.not.found.id", id)));
    }

    /** Return a single department by name. */
    @Transactional(readOnly = true)
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(
                        msg.get("department.not.found.name", name)));
    }

    /** Update name and/or description of an existing department. */
    public Department updateDepartment(Long id, Department updated) {
        Department existing = getDepartmentById(id);

        validateDepartment(updated);

        if (!existing.getName().equalsIgnoreCase(updated.getName().trim()) &&
                departmentRepository.existsByName(updated.getName().trim())) {
            throw new IllegalArgumentException(
                    msg.get("department.name.duplicate", updated.getName()));
        }

        existing.setName(updated.getName().trim());
        existing.setDescription(updated.getDescription());
        return departmentRepository.save(existing);
    }

    /**
     * Delete a department by id.
     * Throws IllegalStateException if any employee is still assigned to it.
     */
    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);

        if (employeeRepository.existsByDepartment(department)) {
            throw new IllegalStateException(
                    msg.get("department.delete.has.employees", department.getName()));
        }

        departmentRepository.delete(department);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void validateDepartment(Department department) {
        if (department.getName() == null || department.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(msg.get("department.name.required"));
        }
    }
}
