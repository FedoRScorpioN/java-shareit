package ru.practicum.shareit.user;

import java.util.List;

public interface UserRepository {
    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(Long id);
}