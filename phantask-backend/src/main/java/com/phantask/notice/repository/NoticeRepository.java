package com.phantask.notice.repository;

import com.phantask.notice.entity.Notice;
import com.phantask.notice.entity.NoticePriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

	List<Notice> findByPriority(NoticePriority priority);

	@Query("SELECT n FROM Notice n JOIN n.targetRoles r WHERE r IN :roles")
	List<Notice> findByTargetRolesIn(@Param("roles") List<String> roles);

	@Query("SELECT n FROM Notice n JOIN n.targetRoles r WHERE r IN :roles AND n.priority = :priority")
	List<Notice> findByTargetRolesInAndPriority(@Param("roles") List<String> roles,
			@Param("priority") NoticePriority priority);
}
