package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validator.Create;
import ru.practicum.shareit.validator.Update;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    public static final String headerUserId = "X-Sharer-User-Id";
    public static final String PAGE_DEFAULT_FROM = "0";
    public static final String PAGE_DEFAULT_SIZE = "10";
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Validated(Create.class) @RequestBody UserDto userDto) {
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @Validated(Update.class) @RequestBody UserDto userDto) {
        return userClient.updateUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userClient.deleteUser(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUser() {
        return userClient.getAllUser();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getByIdUser(@PathVariable Long id) {
        return userClient.getByIdUser(id);
    }
}
