package com.securearchive.archive.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDepartmentMembershipRepository extends JpaRepository<UserDepartmentMembership, Long> {
    List<UserDepartmentMembership> findByUser_Id(Long userId);

    List<UserDepartmentMembership> findByDepartment_Id(Long departmentId);

    Optional<UserDepartmentMembership> findByUser_IdAndDepartment_Id(Long userId, Long departmentId);
    
}