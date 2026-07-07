package com.securearchive.archive.department;

import com.securearchive.archive.department.dto.DepartmentRankResponse;
import com.securearchive.archive.department.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final DepartmentRankRepository departmentRankRepository;

    public List<DepartmentResponse> getDepartments() {
        return departmentRepository.findAll()
            .stream()
            .map(DepartmentResponse::from)
            .toList();
    }

    public List<DepartmentRankResponse> getDepartmentRanks(Long departmentId) {
        return departmentRankRepository.findByDepartment_Id(departmentId)
                .stream()
                .map(DepartmentRankResponse::from)
                .toList();
    }
}
