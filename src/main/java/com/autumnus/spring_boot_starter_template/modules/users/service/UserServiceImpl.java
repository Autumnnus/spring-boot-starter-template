package com.autumnus.spring_boot_starter_template.modules.users.service;

import com.autumnus.spring_boot_starter_template.common.exception.ResourceNotFoundException;
import com.autumnus.spring_boot_starter_template.modules.auth.dto.UpdateProfileRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserCreateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserResponse;
import com.autumnus.spring_boot_starter_template.modules.users.dto.UserUpdateRequest;
import com.autumnus.spring_boot_starter_template.modules.users.entity.User;
import com.autumnus.spring_boot_starter_template.modules.users.entity.Role;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserRole;
import com.autumnus.spring_boot_starter_template.modules.users.entity.UserStatus;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserAccountLockedException;
import com.autumnus.spring_boot_starter_template.modules.users.service.UserAlreadyExistsException;
import com.autumnus.spring_boot_starter_template.modules.users.mapper.UserMapper;
import com.autumnus.spring_boot_starter_template.modules.users.repository.RoleRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserRepository;
import com.autumnus.spring_boot_starter_template.modules.users.repository.UserSpecifications;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable, UserRole role, UserStatus status) {
        final Specification<User> specification = Specification.where(UserSpecifications.withRole(role))
                .and(UserSpecifications.withStatus(status));
        return userRepository.findAll(specification, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        final User user = userRepository.findByUuid(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new UserAlreadyExistsException("Email is already in use");
        });
        userRepository.findByUsername(request.username()).ifPresent(existing -> {
            throw new UserAlreadyExistsException("Username is already in use");
        });
        final User user = userMapper.toEntity(request);
        final Set<String> roles = (request.roles() == null || request.roles().isEmpty())
                ? Set.of("USER")
                : request.roles();
        user.setRoles(fetchRoles(roles));
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setPasswordChangedAt(Instant.now());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        final User user = userRepository.findByUuid(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Email is already in use");
            });
            user.setEmail(request.email());
        }
        if (request.username() != null && !request.username().equalsIgnoreCase(user.getUsername())) {
            userRepository.findByUsername(request.username()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Username is already in use");
            });
            user.setUsername(request.username());
        }
        userMapper.updateEntity(request, user);
        if (request.roles() != null) {
            user.setRoles(fetchRoles(request.roles()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(UUID id) {
        final User user = userRepository.findByUuid(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        final User user = findById(userId);
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Email is already in use");
            });
            user.setEmail(request.email());
        }
        if (request.username() != null && !request.username().equalsIgnoreCase(user.getUsername())) {
            userRepository.findByUsername(request.username()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Username is already in use");
            });
            user.setUsername(request.username());
        }
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void activateUser(Long userId) {
        final User user = findById(userId);
        user.setActive(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        final User user = findById(userId);
        user.setActive(false);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public void checkAccountLocked(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new UserAccountLockedException("Account is locked until " + user.getLockedUntil());
        }
    }

    @Override
    public void incrementFailedAttempts(User user) {
        final int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= 5) {
            user.setLockedUntil(Instant.now().plusSeconds(900));
        }
        userRepository.save(user);
    }

    @Override
    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    private Set<Role> fetchRoles(Set<String> roles) {
        return roles.stream()
                .map(name -> roleRepository.findByName(name.toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name)))
                .collect(Collectors.toSet());
    }
}
