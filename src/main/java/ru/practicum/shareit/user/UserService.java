package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long id, UserDto userDto);

    Boolean deleteUser(Long id);

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);
}
