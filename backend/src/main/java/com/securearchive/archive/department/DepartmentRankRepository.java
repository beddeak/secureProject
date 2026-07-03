package com.securearchive.archive.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface DepartmentRankRepository extends JpaRepository<DepartmentRank, Long>{

    List<DepartmentRank> findByDepartment_Id(Long departmentId);

    Optional<DepartmentRank> findByDepartment_IdAndCode(Long departmentId, String code);

    boolean existsByDepartment_IdAndCode(Long departmentId, String code);

    boolean existsByDepartment_IdAndName(Long departmentId, String name);
}