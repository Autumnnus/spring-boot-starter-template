package com.autumnus.spring_boot_starter_template.common.logging.repository;

import com.autumnus.spring_boot_starter_template.common.logging.document.ApplicationLog;

public interface ApplicationLogRepository {

    void save(ApplicationLog log);
}
