package com.autumnus.spring_boot_starter_template.common.security;

import org.springframework.stereotype.Component;

@Component
public class OwnershipGuard {

    public boolean isOwner(Long ownerId) {
        if (ownerId == null) {
            return false;
        }
        return SecurityUtils.getCurrentUserId()
                .map(ownerId::equals)
                .orElse(false);
    }
}
