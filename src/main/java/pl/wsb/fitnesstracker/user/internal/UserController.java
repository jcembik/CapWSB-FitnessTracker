package pl.wsb.fitnesstracker.user.internal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserDto;
import pl.wsb.fitnesstracker.user.api.UserEmailDto;
import pl.wsb.fitnesstracker.user.api.UserSimpleDto;

import java.time.LocalDate;
import java.util.List;

/**
 * UserController is responsible for handling HTTP requests related to user operations.
 * It provides endpoints for retrieving, creating, updating, and deleting users.
 */
@RestController
@RequestMapping("/v1/users")
class UserController {

    private final UserServiceImpl userService;

    private final UserMapper userMapper;

    public UserController(UserServiceImpl userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves all users with basic information.
     *
     * @return A list of {@link UserSimpleDto} containing basic user details.
     */
    @GetMapping
    public List<UserSimpleDto> getAllUsers() {
        return userService.findAllUsers()
                .stream()
                .map(userMapper::toSimpleDto)
                .toList();
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The {@link UserDto} containing user details.
     */
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        return userService.getUser(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * Creates a new user.
     *
     * @param userDto The {@link UserDto} containing the new user's information.
     * @return The created {@link UserDto}.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@RequestBody UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(userService.createUser(user));
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    /**
     * Searches for users by email fragment (case-insensitive).
     *
     * @param email The email fragment to search for.
     * @return A list of {@link UserEmailDto} matching the search criteria.
     */
    @GetMapping("/email")
    public List<UserEmailDto> getUsersByEmailFragment(@RequestParam String email) {
        return userService.findUsersByEmailFragment(email)
                .stream()
                .map(userMapper::toEmailDto)
                .toList();
    }

    /**
     * Searches for users older than a specified date.
     *
     * @param time The date to compare birthdates against. Users born before this date are returned.
     * @return A list of {@link UserDto} for users older than the specified date.
     */
    @GetMapping("/older/{time}")
    public List<UserDto> getUsersOlderThan(@PathVariable LocalDate time) {
        return userService.findUsersOlderThan(time)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Updates an existing user.
     *
     * @param id      The ID of the user to update.
     * @param userDto The {@link UserDto} containing updated fields.
     * @return The updated {@link UserDto}.
     */
    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        User user = userService.getUser(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (userDto.firstName() != null) {
            user.setFirstName(userDto.firstName());
        }
        if (userDto.lastName() != null) {
            user.setLastName(userDto.lastName());
        }
        if (userDto.birthdate() != null) {
            user.setBirthdate(userDto.birthdate());
        }
        if (userDto.email() != null) {
            user.setEmail(userDto.email());
        }
        
        return userMapper.toDto(userService.updateUser(user));
    }
}
