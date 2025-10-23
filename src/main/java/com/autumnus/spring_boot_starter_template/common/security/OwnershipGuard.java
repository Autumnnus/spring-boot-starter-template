package com.autumnus.spring_boot_starter_template.common.security;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OwnershipGuard {

    public boolean isOwner(UUID ownerId) {
        return SecurityUtils.getCurrentUserId()
                .map(id -> safeParse(id, ownerId))
                .orElse(false);
    }

    private boolean safeParse(String candidate, UUID ownerId) {
        try {
            return UUID.fromString(candidate).equals(ownerId);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
