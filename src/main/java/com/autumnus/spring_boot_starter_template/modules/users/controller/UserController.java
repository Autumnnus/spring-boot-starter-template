package com.autumnus.spring_boot_starter_template.modules.users.controller;

import com.autumnus.spring_boot_starter_template.common.api.ApiResponse;
import com.autumnus.spring_boot_starter_template.common.api.PaginationMeta;
import com.autumnus.spring_boot_starter_template.common.context.RequestContextHolder;
import com.autumnus.spring_boot_starter_template.common.idempotency.Idempotent;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaStorageException;
import com.autumnus.spring_boot_starter_template.modules.users.dto.ProfilePhotoUploadCommand;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserPrincipal;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
            @RequestParam(required = false) Boolean active
    ) {
        final Page<UserResponse> result = userService.listUsers(pageable, active);
        final PaginationMeta meta = PaginationMeta.builder()
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), result.getContent(), meta);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Override
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        final UserResponse response = userService.getUser(principal.getUserId());
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.isOwner(#id)")
    @Override
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
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

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Override
    public ApiResponse<UserResponse> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        final UserResponse response = userService.updateProfile(principal.getUserId(), request);
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.isOwner(#id)")
    @Override
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        final UserResponse response = userService.updateUser(id, request);
        return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.isOwner(#id)")
    @Override
    public ApiResponse<UserResponse> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            final ProfilePhotoUploadCommand command = new ProfilePhotoUploadCommand(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            final UserResponse response = userService.updateProfilePhoto(id, command);
            return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
        } catch (IOException e) {
            throw new MediaStorageException("Failed to read uploaded file", e);
        }
    }

    @PostMapping(value = "/me/profile-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Override
    public ApiResponse<UserResponse> uploadOwnProfilePhoto(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            final ProfilePhotoUploadCommand command = new ProfilePhotoUploadCommand(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            );
            final UserResponse response = userService.updateProfilePhoto(principal.getUserId(), command);
            return ApiResponse.ok(RequestContextHolder.getContext().getTraceId(), response);
        } catch (IOException e) {
            throw new MediaStorageException("Failed to read uploaded file", e);
        }
    }

    @DeleteMapping("/{id}/profile-photo")
    @PreAuthorize("hasRole('ADMIN') or @ownershipGuard.isOwner(#id)")
    @Override
    public ResponseEntity<Void> deleteProfilePhoto(@PathVariable Long id) {
        userService.removeProfilePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/profile-photo")
    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<Void> deleteOwnProfilePhoto(@AuthenticationPrincipal UserPrincipal principal) {
        userService.removeProfilePhoto(principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}
