package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.*;

@Repository
public class UserRepositoryInMemory implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private Long idMax = 1L;

    @Override
    public User createUser(User user) {
        emailIsAvailable(user);
        user.setId(idMax++);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public User updateUser(User user) {
        User repoUser = getUserById(user.getId());
        if (user.getEmail() != null || repoUser.getEmail().equals(user.getEmail())) {
            emailIsAvailable(user);
            emails.remove(repoUser.getEmail());
            repoUser.setEmail(user.getEmail());
            emails.add(repoUser.getEmail());
        }
        if (user.getName() != null) {
            repoUser.setName(user.getName());
        }
        return users.put(repoUser.getId(), repoUser);
    }

    @Override
    public boolean deleteUser(Long id) {
        if (users.containsKey(id)) {
            emails.remove(users.get(id).getEmail());
            users.remove(id);
        }
        return true;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new NotFoundException("Пользователя с указанным ID не существует.");
        }
    }

    private void emailIsAvailable(User user) {
        if (emails.contains(user.getEmail())) {
            throw new ConflictException("Данный email уже зарегистрирован.");
        }
    }
}