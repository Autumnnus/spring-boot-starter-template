package com.autumnus.spring_boot_starter_template.modules.users.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "User management endpoints")
@RequestMapping("/api/v1/users")
public interface UserApi {

    @Operation(summary = "List users", description = "Get a paginated list of users with optional filters.")
    @GetMapping
    ApiResponse<?> listUsers(
            @Parameter(hidden = true) @PageableDefault Pageable pageable,
            @RequestParam(required = false) RoleName role,
            @RequestParam(required = false) Boolean active
    );

    @Operation(summary = "Get user", description = "Retrieve details of a specific user by ID.")
    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable UUID id);

    @Operation(summary = "Create user", description = "Create a new user.")
    @PostMapping
    ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request);

    @Operation(summary = "Update user", description = "Update an existing user.")
    @PutMapping("/{id}")
    ApiResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request
    );

    @Operation(summary = "Delete user", description = "Delete a user by ID.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable UUID id);
}