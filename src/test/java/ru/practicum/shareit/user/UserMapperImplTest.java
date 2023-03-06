package ru.practicum.shareit.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserMapperImplTest {
    @InjectMocks
    private UserMapperImpl userMapper;
    private final User user = User.builder()
            .id(1L)
            .name("User 1")
            .email("mail1@yandex.ru")
            .build();
    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("User 1")
            .email("mail1@yandex.ru")
            .build();

    @Nested
    class ToUserDto {
        @Test
        void shouldReturnUserDto() {
            UserDto result = userMapper.toUserDto(user);
            assertEquals(user.getId(), result.getId());
            assertEquals(user.getName(), result.getName());
            assertEquals(user.getEmail(), result.getEmail());
        }

        @Test
        void shouldReturnNull() {
            UserDto result = userMapper.toUserDto(null);
            assertNull(result);
        }
    }

    @Nested
    class ToUser {
        @Test
        void shouldReturnUser() {
            User result = userMapper.toUser(userDto);
            assertEquals(userDto.getId(), result.getId());
            assertEquals(userDto.getName(), result.getName());
            assertEquals(userDto.getEmail(), result.getEmail());
        }

        @Test
        void shouldReturnNull() {
            User result = userMapper.toUser(null);
            assertNull(result);
        }
    }
}