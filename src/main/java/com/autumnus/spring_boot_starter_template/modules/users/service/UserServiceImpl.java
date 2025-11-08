package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.common.exception.ResourceNotFoundException;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.Auditable;
import com.autumnus.spring_boot_starter_template.common.logging.context.AuditContextHolder;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaAsset;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaStorageException;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaValidationException;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaKind;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaManifest;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaUpload;
import com.autumnus.spring_boot_starter_template.common.storage.service.MediaStorageService;
import com.autumnus.spring_boot_starter_template.modules.users.dto.ProfilePhotoUploadCommand;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRoleAssignment;
import com.autumnus.spring_boot_starter_template.modules.users.mapper.UserMapper;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserSpecifications;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MediaStorageService mediaStorageService;
    private final ObjectMapper objectMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            MediaStorageService mediaStorageService,
            ObjectMapper objectMapper
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.mediaStorageService = mediaStorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable, RoleName role, Boolean active) {
        final Specification<User> specification = Specification.where(UserSpecifications.withRole(role))
                .and(UserSpecifications.withActive(active));
        return userRepository.findAll(specification, pageable)
                .map(user -> userMapper.toResponse(user, userMapper.extractRoleNames(user)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID uuid) {
        if (command == null || command.content() == null || command.content().length == 0) {
            throw new MediaValidationException("Profile photo file is required");
        }
        final User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user, userMapper.extractRoleNames(user));
    }

    @Override
    @Auditable(entityType = "USER", action = AuditAction.CREATE, captureNewValue = true)
    public UserResponse createUser(UserCreateRequest request) {
        validateEmailUniqueness(request.email(), null);
        validateUsernameUniqueness(request.username(), null);
        final User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(request.active() == null || request.active());
        user.setPasswordChangedAt(Instant.now());
        assignRoles(user, request.roles(), null);
        final User saved = userRepository.save(user);
        AuditContextHolder.setEntityId(saved.getUuid().toString());
        AuditContextHolder.setNewValue(userMapper.toResponse(saved, userMapper.extractRoleNames(saved)));
        return userMapper.toResponse(saved, userMapper.extractRoleNames(saved));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#uuid"
    )
    public UserResponse updateUser(UUID uuid, UserUpdateRequest request) {
        final User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AuditContextHolder.setEntityId(user.getUuid().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        if (request.email() != null && !Objects.equals(request.email(), user.getEmail())) {
            validateEmailUniqueness(request.email(), user.getId());
        }
        if (request.username() != null && !Objects.equals(request.username(), user.getUsername())) {
            validateUsernameUniqueness(request.username(), user.getId());
        }
        userMapper.updateEntity(request, user);
        if (request.roles() != null) {
            assignRoles(user, request.roles(), null);
        }
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved, userMapper.extractRoleNames(saved)));
        return userMapper.toResponse(saved, userMapper.extractRoleNames(saved));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.DELETE,
            captureOldValue = true,
            entityIdExpression = "#uuid"
    )
    public void deleteUser(UUID uuid) {
        final User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AuditContextHolder.setEntityId(user.getUuid().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        user.markDeleted();
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findEntityByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findEntityByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public User findEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#userId"
    )
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        final User user = findEntityById(userId);
        AuditContextHolder.setEntityId(user.getUuid().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        if (request.email() != null && !Objects.equals(request.email(), user.getEmail())) {
            validateEmailUniqueness(request.email(), user.getId());
            user.setEmail(request.email());
        }
        if (request.username() != null && !Objects.equals(request.username(), user.getUsername())) {
            validateUsernameUniqueness(request.username(), user.getId());
            user.setUsername(request.username());
        }
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved, userMapper.extractRoleNames(saved)));
        return userMapper.toResponse(saved, userMapper.extractRoleNames(saved));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#uuid"
    )
    public UserResponse updateProfilePhoto(UUID uuid, ProfilePhotoUploadCommand command) {
        final User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AuditContextHolder.setEntityId(user.getUuid().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        final MediaManifest existingManifest = parseManifest(user.getProfilePhotoManifest());
        final MediaUpload upload = new MediaUpload(command.originalFilename(), command.contentType(), command.content());
        final MediaAsset asset = mediaStorageService.replace(existingManifest, MediaKind.IMAGE, "avatar", upload);
        user.setProfilePhotoManifest(writeManifest(asset.manifest()));
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved, userMapper.extractRoleNames(saved)));
        return userMapper.toResponse(saved, userMapper.extractRoleNames(saved));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#uuid"
    )
    public void removeProfilePhoto(UUID uuid) {
        final User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        AuditContextHolder.setEntityId(user.getUuid().toString());
        if (user.getProfilePhotoManifest() == null) {
            return;
        }
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        final MediaManifest manifest = parseManifest(user.getProfilePhotoManifest());
        mediaStorageService.delete(manifest);
        user.setProfilePhotoManifest(null);
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved, userMapper.extractRoleNames(saved)));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#userId"
    )
    public void activateUser(Long userId) {
        final User user = findEntityById(userId);
        AuditContextHolder.setEntityId(user.getUuid().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        user.setActive(true);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#userId"
    )
    public void deactivateUser(Long userId) {
        final User user = findEntityById(userId);
        AuditContextHolder.setEntityId(user.getUuid().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
        user.setActive(false);
        userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(user, userMapper.extractRoleNames(user)));
    }

    @Override
    public void checkAccountLocked(User user) {
        if (!user.isActive()) {
            throw new UserServiceValidationException("USER_INACTIVE", "User account is deactivated");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new UserServiceValidationException("USER_LOCKED", "User account is temporarily locked");
        }
    }

    @Override
    public void incrementFailedAttempts(User user) {
        final int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));
        }
        userRepository.save(user);
    }

    @Override
    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    private MediaManifest parseManifest(String manifestJson) {
        if (manifestJson == null) {
            return null;
        }
        try {
            return objectMapper.readValue(manifestJson, MediaManifest.class);
        } catch (JsonProcessingException e) {
            throw new MediaStorageException("Failed to parse stored media manifest", e);
        }
    }

    private String writeManifest(MediaManifest manifest) {
        try {
            return objectMapper.writeValueAsString(manifest);
        } catch (JsonProcessingException e) {
            throw new MediaStorageException("Failed to persist media manifest", e);
        }
    }

    private void assignRoles(User user, Set<RoleName> roles, User assignedBy) {
        final Set<RoleName> targetRoles = (roles == null || roles.isEmpty())
                ? Set.of(RoleName.USER)
                : roles;
        user.getRoleAssignments().clear();
        for (RoleName roleName : targetRoles) {
            final Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
            final UserRoleAssignment assignment = new UserRoleAssignment();
            assignment.setUser(user);
            assignment.setRole(role);
            assignment.setAssignedAt(Instant.now());
            assignment.setAssignedBy(assignedBy);
            user.getRoleAssignments().add(assignment);
        }
    }

    private void validateEmailUniqueness(String email, Long excludeId) {
        if (email == null) {
            return;
        }
        final Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new UserServiceValidationException("EMAIL_IN_USE", "Email is already in use");
        }
    }

    private void validateUsernameUniqueness(String username, Long excludeId) {
        if (username == null) {
            return;
        }
        final Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent() && (excludeId == null || !existing.get().getId().equals(excludeId))) {
            throw new UserServiceValidationException("USERNAME_IN_USE", "Username is already in use");
        }
    }
}
