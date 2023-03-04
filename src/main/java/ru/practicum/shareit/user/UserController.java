package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.validator.Create;
import ru.practicum.shareit.validator.Update;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    public static final String headerUserId = "X-Sharer-User-Id";
    public static final String PAGE_DEFAULT_FROM = "0";
    public static final String PAGE_DEFAULT_SIZE = "10";
    public final UserService userService;

    @PostMapping
    public UserDto createUser(@Validated(Create.class) @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        return userService.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @GetMapping
    public List<UserDto> getAllUser() {
        return userService.getAllUser();
    }

    @GetMapping("/{id}")
    public UserDto getByIdUser(@PathVariable Long id) {
        return userService.getByIdUser(id);
    }
}