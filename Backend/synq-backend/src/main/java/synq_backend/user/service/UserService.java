package synq_backend.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import synq_backend.common.exception.ResourceNotFoundException;
import synq_backend.user.dto.UserDTO;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;

import synq_backend.user.dto.CreateUserRequest;

import java.util.UUID;

// Handles User business logic and database operations.
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //constructor injection
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get an existing User by ID.
    public UserDTO getUserById(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create DTO for the API response.
        UserDTO dto = new UserDTO();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setStatus(user.getStatus());

        return dto;
    }

    // Create a new User from the incoming request.
    public UserDTO createUser(CreateUserRequest request){

        // Create User entity from the incoming request.
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName());
        user.setStatus("OFFLINE");

        User savedUser = userRepository.save(user);

        // Create DTO for the API response.
        UserDTO dto = new UserDTO();

        dto.setId(savedUser.getId());
        dto.setUsername(savedUser.getUsername());
        dto.setEmail(savedUser.getEmail());
        dto.setDisplayName(savedUser.getDisplayName());
        dto.setProfileImageUrl(savedUser.getProfileImageUrl());
        dto.setStatus(savedUser.getStatus());

        return dto;
    }
}
