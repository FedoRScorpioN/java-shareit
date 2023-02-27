package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Добавление пользователя {}", userDto);
        return userMapper.toUserDto(userRepository.createUser(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя {} с id {}.", userDto, id);
        userDto.setId(id);
        return userMapper.toUserDto(userRepository.updateUser(userMapper.toUser(userDto)));
    }

    @Override
    public Boolean deleteUser(Long id) {
        log.info("Удаление пользователя с id {}", id);
        return userRepository.deleteUser(id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Вывод всех пользователей.");
        return userRepository.getAllUsers().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Вывод пользователя с id {}.", id);
        return userMapper.toUserDto(userRepository.getUserById(id));
    }
}