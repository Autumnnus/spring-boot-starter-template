package com.autumnus.spring_boot_starter_template.modules.users.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.common.api.PaginationMeta;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.idempotency.Idempotent;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ApiResponse<?> listUsers(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) RoleName role,
            @RequestParam(required = false) Boolean active
    ) {
        final Page<UserResponse> result = userService.listUsers(pageable, role, active);
        final PaginationMeta meta = PaginationMeta.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), result.getContent(), meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.isOwner(#id)")
    @Override
    public ApiResponse<UserResponse> getUser(@PathVariable UUID id) {
        final UserResponse response = userService.getUser(id);
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @PostMapping
    @Idempotent
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @Override
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        final UserResponse response = userService.createUser(request);
        final ApiResponse<UserResponse> payload = ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
        return ResponseEntity.status(201).body(payload);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.isOwner(#id)")
    @Override
    public ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        final UserResponse response = userService.updateUser(id, request);
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
