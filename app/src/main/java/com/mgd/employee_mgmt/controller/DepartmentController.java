package com.mgd.employee_mgmt.controller;

import com.mgd.employee_mgmt.model.Department;
import com.mgd.employee_mgmt.service.DepartmentService;
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
@RequestMapping("${api.departments.base}")
@CrossOrigin(origins = "${cors.allowed.origins}")
@PropertySource("classpath:urls.properties")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final MessageUtil msg;

    @Autowired
    public DepartmentController(DepartmentService departmentService, MessageUtil msg) {
        this.departmentService = departmentService;
        this.msg = msg;
    }

    // POST /api/departments
    @PostMapping
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        Department saved = departmentService.createDepartment(department);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // GET /api/departments
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    // GET /api/departments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    // GET /api/departments/name/{name}
    @GetMapping("${api.departments.by.name}")
    public ResponseEntity<Department> getDepartmentByName(@PathVariable String name) {
        return ResponseEntity.ok(departmentService.getDepartmentByName(name));
    }

    // PUT /api/departments/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department department) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, department));
    }

    // DELETE /api/departments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", msg.get("department.deleted.success"));
        return ResponseEntity.ok(response);
    }
}
