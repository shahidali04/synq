package synq_backend.user.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import synq_backend.user.dto.CreateUserRequest;
import synq_backend.user.dto.UserDTO;
import synq_backend.user.entity.User;
import synq_backend.user.service.UserService;

import java.util.UUID;

// Handles REST APIs related to users.
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserDTO getCurrentUser(Authentication authentication){

        User user = (User) authentication.getPrincipal();

        return userService.getUserById(user.getId());
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id){
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request){
        return userService.createUser(request);
    }


}
