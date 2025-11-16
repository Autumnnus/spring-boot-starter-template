package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.common.exception.ResourceNotFoundException;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.AuditAction;
import com.autumnus.spring_boot_starter_template.common.logging.annotation.Auditable;
import com.autumnus.spring_boot_starter_template.common.logging.context.AuditContextHolder;
import com.autumnus.spring_boot_starter_template.common.messaging.NotificationProducer;
import com.autumnus.spring_boot_starter_template.common.messaging.dto.NotificationMessage;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaAsset;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaStorageException;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaKind;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaManifest;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaUpload;
import com.autumnus.spring_boot_starter_template.common.storage.service.MediaStorageService;
import com.autumnus.spring_boot_starter_template.modules.users.dto.*;
import com.autumnus.spring_boot_starter_template.modules.users.entity.RoleName;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.mapper.UserMapper;
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

import java.util.Objects;
import java.util.Optional;

/**
 * User service implementation handling application-specific user data.
 * Authentication, authorization and user management is handled by Keycloak.
 * This service only manages application-specific user metadata.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MediaStorageService mediaStorageService;
    private final ObjectMapper objectMapper;
    private final NotificationProducer notificationProducer;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            MediaStorageService mediaStorageService,
            ObjectMapper objectMapper,
            NotificationProducer notificationProducer
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.mediaStorageService = mediaStorageService;
        this.objectMapper = objectMapper;
        this.notificationProducer = notificationProducer;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable, RoleName role, Boolean active) {
        final Specification<User> specification = Specification.where(UserSpecifications.withActive(active));
        return userRepository.findAll(specification, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        final User user = findEntityById(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Auditable(entityType = "USER", action = AuditAction.CREATE, captureNewValue = true)
    public UserResponse createUser(UserCreateRequest request) {
        validateEmailUniqueness(request.email(), null);
        validateUsernameUniqueness(request.username(), null);
        final User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        // Password hash is deprecated - kept for backward compatibility
        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        user.setActive(request.active() == null || request.active());
        final User saved = userRepository.save(user);
        AuditContextHolder.setEntityId(saved.getId().toString());
        AuditContextHolder.setNewValue(userMapper.toResponse(saved));
        notificationProducer.send(new NotificationMessage(
                saved.getId(),
                "Welcome to Autumnus",
                "Hi %s, your account is ready to use.".formatted(saved.getUsername()),
                NotificationMessage.NotificationType.SUCCESS
        ));
        return userMapper.toResponse(saved);
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#id"
    )
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        final User user = findEntityById(id);
        AuditContextHolder.setEntityId(user.getId().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
        if (request.email() != null && !Objects.equals(request.email(), user.getEmail())) {
            validateEmailUniqueness(request.email(), user.getId());
        }
        if (request.username() != null && !Objects.equals(request.username(), user.getUsername())) {
            validateUsernameUniqueness(request.username(), user.getId());
        }
        userMapper.updateEntity(request, user);
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved));
        return userMapper.toResponse(saved);
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.DELETE,
            captureOldValue = true,
            entityIdExpression = "#id"
    )
    public void deleteUser(Long id) {
        final User user = findEntityById(id);
        AuditContextHolder.setEntityId(user.getId().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
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
        AuditContextHolder.setEntityId(user.getId().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
        if (request.email() != null && !Objects.equals(request.email(), user.getEmail())) {
            validateEmailUniqueness(request.email(), user.getId());
            user.setEmail(request.email());
        }
        if (request.username() != null && !Objects.equals(request.username(), user.getUsername())) {
            validateUsernameUniqueness(request.username(), user.getId());
            user.setUsername(request.username());
        }
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved));
        return userMapper.toResponse(saved);
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#id"
    )
    public UserResponse updateProfilePhoto(Long id, ProfilePhotoUploadCommand command) {
        final User user = findEntityById(id);
        AuditContextHolder.setEntityId(user.getId().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
        final MediaManifest existingManifest = parseManifest(user.getProfilePhotoManifest());
        final MediaUpload upload = new MediaUpload(command.originalFilename(), command.contentType(), command.content());
        final MediaAsset asset = mediaStorageService.replace(existingManifest, MediaKind.IMAGE, "avatar", upload);
        user.setProfilePhotoManifest(writeManifest(asset.manifest()));
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved));
        return userMapper.toResponse(saved);
    }

    @Override
    @Auditable(
            entityType = "USER",
            action = AuditAction.UPDATE,
            captureOldValue = true,
            captureNewValue = true,
            entityIdExpression = "#id"
    )
    public void removeProfilePhoto(Long id) {
        final User user = findEntityById(id);
        AuditContextHolder.setEntityId(user.getId().toString());
        if (user.getProfilePhotoManifest() == null) {
            return;
        }
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
        final MediaManifest manifest = parseManifest(user.getProfilePhotoManifest());
        mediaStorageService.delete(manifest);
        user.setProfilePhotoManifest(null);
        final User saved = userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(saved));
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
        AuditContextHolder.setEntityId(user.getId().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
        user.setActive(true);
        userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(user));
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
        AuditContextHolder.setEntityId(user.getId().toString());
        AuditContextHolder.setOldValue(userMapper.toResponse(user));
        user.setActive(false);
        userRepository.save(user);
        AuditContextHolder.setNewValue(userMapper.toResponse(user));
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
