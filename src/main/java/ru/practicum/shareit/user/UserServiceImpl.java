package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Добавление пользователя {}", userDto);
        return userMapper.toUserDto(userRepository.save(userMapper.toUser(userDto)));
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя {} с id {}.", userDto, id);
        User repoUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует."));
        if (userDto.getEmail() != null) {
            repoUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            repoUser.setName(userDto.getName());
        }
        return userMapper.toUserDto(userRepository.save(repoUser));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id {}", id);
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> getAllUser() {
        log.info("Вывод всех пользователей.");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getByIdUser(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return userMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует.")));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователя с таким id не существует."));
    }
}