package com.autumnus.spring_boot_starter_template.modules.users.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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

    @Operation(summary = "Get current user", description = "Retrieve the authenticated user's profile using the access token.")
    @GetMapping("/me")
    ApiResponse<UserResponse> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal);

    @Operation(summary = "Get user", description = "Retrieve details of a specific user by ID.")
    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable Long id);

    @Operation(summary = "Create user", description = "Create a new user.")
    @PostMapping
    ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request);

    @Operation(summary = "Update current user", description = "Update the authenticated user's profile without providing an ID.")
    @PutMapping("/me")
    ApiResponse<UserResponse> updateCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    );

    @Operation(summary = "Update user", description = "Update an existing user.")
    @PutMapping("/{id}")
    ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    );

    @Operation(summary = "Delete user", description = "Delete a user by ID.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @Operation(
            summary = "Upload profile photo",
            description = "Upload or replace the profile photo for the given user."
    )
    @PostMapping(value = "/{id}/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<UserResponse> uploadProfilePhoto(
            @PathVariable Long id,
            @Parameter(
                    description = "Profile image file",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    );

    @Operation(
            summary = "Upload own profile photo",
            description = "Upload or replace the authenticated user's profile photo without specifying an ID."
    )
    @PostMapping(value = "/me/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<UserResponse> uploadOwnProfilePhoto(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(
                    description = "Profile image file",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart("file") MultipartFile file
    );


    @Operation(summary = "Delete profile photo", description = "Remove the profile photo for the given user.")
    @DeleteMapping("/{id}/profile-photo")
    ResponseEntity<Void> deleteProfilePhoto(@PathVariable Long id);

    @Operation(summary = "Delete own profile photo", description = "Remove the authenticated user's profile photo using the token.")
    @DeleteMapping("/me/profile-photo")
    ResponseEntity<Void> deleteOwnProfilePhoto(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal);
}