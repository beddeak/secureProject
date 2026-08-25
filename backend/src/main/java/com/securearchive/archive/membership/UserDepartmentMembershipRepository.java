package com.securearchive.archive.membership;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDepartmentMembershipRepository extends JpaRepository<UserDepartmentMembership, Long> {
    List<UserDepartmentMembership> findByUser_Id(Long userId);

    List<UserDepartmentMembership> findByDepartment_Id(Long departmentId);

    Optional<UserDepartmentMembership> findByUser_IdAndDepartment_Id(Long userId, Long departmentId);

    boolean existsByUser_IdAndDepartment_IdAndLeftAtIsNull(Long userId, Long departmentId);

    @Query("""
        select count(m)
        from UserDepartmentMembership m
        where m.user.id = :userId
          and m.department.id = :departmentId
          and m.departmentRank.department.id = :departmentId
          and m.leftAt is null
          and m.departmentRank.levelOrder >= :minimumLevel
        """)
    long countActiveMembershipsWithMinimumRank(
        @Param("userId") Long userId,
        @Param("departmentId") Long departmentId,
        @Param("minimumLevel") Integer minimumLevel
    );
}
