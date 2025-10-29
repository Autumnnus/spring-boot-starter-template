package com.autumnus.spring_boot_starter_template.common.logging.repository;

import com.autumnus.spring_boot_starter_template.common.logging.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
