package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    @MockBean
    private UserService userService;
    private final UserDto userDto1 = UserDto.builder()
            .id(1L)
            .name("User 1")
            .email("mail1@yandex.ru")
            .build();
    private final UserDto userDto2 = UserDto.builder()
            .id(2L)
            .name("User 2")
            .email("mail2@yandex.ru")
            .build();
    private UserDto userDtoToUpdate;
    private UserDto userDtoUpdate;

    @BeforeEach
    public void beforeEach() {
        userDtoToUpdate = UserDto.builder()
                .name("Update test User 1")
                .email("UpdateMail1@yandex.ru")
                .build();
        userDtoUpdate = UserDto.builder()
                .id(1L)
                .name("Updated test User 1")
                .email("UpdatedMail1@yandex.ru")
                .build();
    }

    @Nested
    class CreateUser {
        @Test
        void shouldCreate() throws Exception {
            when(userService.createUser(any(UserDto.class))).thenReturn(userDto1);
            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(userDto1)));
            verify(userService, times(1)).createUser(any(UserDto.class));
        }

        @Test
        void shouldThrowExceptionIfEmailIsNull() throws Exception {
            userDto1.setEmail(null);
            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any());
        }

        @Test
        void shouldThrowExceptionIfEmailIsEmpty() throws Exception {
            userDto1.setEmail("");
            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any());
        }

        @Test
        void shouldThrowExceptionIfEmailIsBlank() throws Exception {
            userDto1.setEmail(" ");
            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any());
        }

        @Test
        void shouldThrowExceptionIfIsNotEmail() throws Exception {
            userDto1.setEmail("mail1yandex.ru");
            mvc.perform(post("/users")
                            .content(mapper.writeValueAsString(userDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(userService, never()).createUser(any());
        }
    }

    @Nested
    class GetAllUser {
        @Test
        void shouldGet() throws Exception {
            when(userService.getAllUser()).thenReturn(List.of(userDto1, userDto2));
            mvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(userDto1, userDto2))));
            verify(userService, times(1)).getAllUser();
        }

        @Test
        void shouldGetIfEmpty() throws Exception {
            when(userService.getAllUser()).thenReturn(List.of());
            mvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of())));
            verify(userService, times(1)).getAllUser();
        }
    }

    @Nested
    class GetByIdUser {
        @Test
        void shouldGet() throws Exception {
            when(userService.getByIdUser(eq(userDto1.getId()))).thenReturn(userDto1);
            mvc.perform(get("/users/{id}", userDto1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(userDto1)));
            verify(userService, times(1)).getByIdUser(eq(userDto1.getId()));
        }
    }

    @Nested
    class UpdateUser {
        @Test
        void shouldPatch() throws Exception {
            when(userService.updateUser(eq(userDtoUpdate.getId()), any(UserDto.class)))
                    .thenReturn(userDtoUpdate);
            mvc.perform(patch("/users/{id}", userDtoUpdate.getId())
                            .content(mapper.writeValueAsString(userDtoToUpdate))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(userDtoUpdate)));
            verify(userService, times(1))
                    .updateUser(eq(userDtoUpdate.getId()), any(UserDto.class));
        }

        @Test
        void shouldThrowExceptionIfNotEmail() throws Exception {
            userDtoToUpdate.setEmail("UpdatedMail1yandex.ru");
            mvc.perform(patch("/users/{id}", userDtoUpdate.getId())
                            .content(mapper.writeValueAsString(userDtoToUpdate))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(userService, never()).updateUser(any(), any());
        }
    }

    @Nested
    class DeleteUser {
        @Test
        public void shouldDelete() throws Exception {
            mvc.perform(delete("/users/{id}", 99L))
                    .andExpect(status().isOk());
            verify(userService, times(1)).deleteUser(99L);
        }
    }
}