package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserBranchesTest {
    private final UserController userController;

    private void checkUserDto(UserDto userDto, UserDto userDtoFromController) {
        assertEquals(userDto.getId(), userDtoFromController.getId());
        assertEquals(userDto.getName(), userDtoFromController.getName());
        assertEquals(userDto.getEmail(), userDtoFromController.getEmail());
    }

    @Nested
    class CreateUser {
        @Test
        void shouldCreate() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            List<UserDto> usersFromController = new ArrayList<>(userController.getAllUser());
            assertEquals(usersFromController.size(), 1);
            UserDto userFromController = usersFromController.get(0);
            checkUserDto(userDto, userFromController);
        }

        @Test
        void shouldThrowExceptionIfExistedEmail() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail@yandex.ru")
                    .build();
            assertThrows(DataIntegrityViolationException.class, () -> userController.createUser(userDto2));
            assertEquals(userController.getAllUser().size(), 1);
            UserDto userFromController = userController.getAllUser().get(0);
            checkUserDto(userDto1, userFromController);
        }
    }

    @Nested
    class GetAllUser {
        @Test
        void shouldGet() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            List<UserDto> usersFromController = userController.getAllUser();
            assertEquals(usersFromController.size(), 2);
            UserDto userFromController1 = usersFromController.get(0);
            UserDto userFromController2 = usersFromController.get(1);
            checkUserDto(userDto1, userFromController1);
            checkUserDto(userDto2, userFromController2);
        }

        @Test
        void shouldGetIfEmpty() {
            List<UserDto> usersFromController = userController.getAllUser();
            assertTrue(usersFromController.isEmpty());
        }
    }

    @Nested
    class GetByIdUser {
        @Test
        void shouldGet() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto usersFromController = userController.getByIdUser(1L);
            checkUserDto(userDto1, usersFromController);
        }

        @Test
        void shouldThrowExceptionIfUserIdNotFound() {
            NotFoundException exception = assertThrows(NotFoundException.class, () -> userController.getByIdUser(10L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
            assertTrue(userController.getAllUser().isEmpty());
        }
    }

    @Nested
    class UpdateUser {
        @Test
        void shouldPatch() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Update test user 1")
                    .email("mail2@yandex.ru")
                    .build();
            userController.updateUser(userDto1.getId(), userDto2);
            List<UserDto> usersFromController = userController.getAllUser();
            assertEquals(usersFromController.size(), 1);
            UserDto userFromController = usersFromController.get(0);
            assertEquals(userFromController.getId(), userDto1.getId());
            assertEquals(userFromController.getName(), userDto2.getName());
            assertEquals(userFromController.getEmail(), userDto2.getEmail());
        }

        @Test
        void shouldThrowExceptionIfExistedEmail() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            UserDto userDto3 = UserDto.builder()
                    .id(3L)
                    .name("Update test user 1")
                    .email("mail2@yandex.ru")
                    .build();
            assertThrows(DataIntegrityViolationException.class, () -> userController.updateUser(userDto1.getId(), userDto3));
            List<UserDto> usersFromController = userController.getAllUser();
            assertEquals(usersFromController.size(), 2);
            UserDto userFromController1 = usersFromController.get(0);
            UserDto userFromController2 = usersFromController.get(1);
            checkUserDto(userDto1, userFromController1);
            checkUserDto(userDto2, userFromController2);
        }
    }

    @Nested
    class DeleteUser {
        @Test
        void shouldDelete() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            List<UserDto> usersFromController = userController.getAllUser();
            assertEquals(usersFromController.size(), 1);
            UserDto userFromController = usersFromController.get(0);
            checkUserDto(userDto1, userFromController);
            userController.deleteUser(userDto1.getId());
            assertTrue(userController.getAllUser().isEmpty());
        }

        @Test
        void shouldDeleteIfUserIdNotFound() {
            assertThrows(EmptyResultDataAccessException.class, () -> userController.deleteUser(10L));
            assertTrue(userController.getAllUser().isEmpty());
        }
    }
}