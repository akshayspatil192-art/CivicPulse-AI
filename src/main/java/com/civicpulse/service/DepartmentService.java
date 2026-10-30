package com.civicpulse.service;

import com.civicpulse.entity.Department;
import com.civicpulse.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + id));
    }
}
