package com.securearchive.archive.department;

import com.securearchive.archive.department.dto.DepartmentRankResponse;
import com.securearchive.archive.department.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor

public class DepartmentController {
    public final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponse> getDepartment() {
        return departmentService.getDepartments();
    }

    @GetMapping("/{departmentId}/ranks")
    public List<DepartmentRankResponse> getDepartmentRanks(@PathVariable Long departmentId) {
        return departmentService.getDepartmentRanks(departmentId);
    }
}