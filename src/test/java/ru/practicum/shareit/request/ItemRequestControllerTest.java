package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.user.UserController.headerUserId;

@WebMvcTest(controllers = ItemRequestController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    @MockBean
    private ItemRequestService itemRequestService;
    private final User user1 = User.builder()
            .id(1L)
            .name("User 1")
            .email("mail1@yandex.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("User 2")
            .email("mail2@yandex.ru")
            .build();
    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(user1.getId())
            .description("Item description")
            .created(LocalDateTime.now())
            .build();
    private final ItemDto itemDto1 = ItemDto.builder()
            .id(1L)
            .name("Item dto 1")
            .description("Item dto 1 description")
            .available(true)
            .ownerId(user1.getId())
            .requestId(1L)
            .build();
    private final ItemDto itemDto2 = ItemDto.builder()
            .id(2L)
            .name("Item dto 2")
            .description("Item dto 2 description")
            .available(false)
            .ownerId(user1.getId())
            .requestId(2L)
            .build();
    private final ItemRequestExtendedDto itemRequestExtendedDto1 = ItemRequestExtendedDto.builder()
            .id(1L)
            .description("Request 1 description")
            .created(LocalDateTime.now())
            .items(List.of(itemDto1, itemDto2))
            .build();
    private final ItemRequestExtendedDto itemRequestExtendedDto2 = ItemRequestExtendedDto.builder()
            .id(2L)
            .description("Request 2 description")
            .created(LocalDateTime.now())
            .items(List.of())
            .build();
    private ItemRequestCreateDto itemRequestCreateDto;
    private int from;
    private int size;

    @BeforeEach
    public void beforeEach() {
        itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("item description")
                .build();
        from = Integer.parseInt(UserController.PAGE_DEFAULT_FROM);
        size = Integer.parseInt(UserController.PAGE_DEFAULT_SIZE);
    }

    @Nested
    class Create {
        @Test
        void shouldCreate() throws Exception {
            when(itemRequestService.create(eq(user1.getId()), any(ItemRequestCreateDto.class)))
                    .thenReturn(itemRequestDto);
            mvc.perform(post("/requests")
                            .header(headerUserId, user1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemRequestDto)));
            verify(itemRequestService, times(1))
                    .create(eq(user1.getId()), any(ItemRequestCreateDto.class));
        }

        @Test
        void shouldThrowExceptionIfNotDescription() throws Exception {
            itemRequestCreateDto.setDescription(null);
            mvc.perform(post("/requests")
                            .header(headerUserId, user1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemRequestService, never()).create(any(), any());
        }

        @Test
        public void shouldThrowExceptionIfDescriptionIsEmpty() throws Exception {
            itemRequestCreateDto.setDescription("");

            mvc.perform(post("/requests")
                            .header(headerUserId, user1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(itemRequestService, never()).create(any(), any());
        }

        @Test
        void shouldThrowExceptionIfDescriptionIsBlank() throws Exception {
            itemRequestCreateDto.setDescription(" ");
            mvc.perform(post("/requests")
                            .header(headerUserId, user1.getId())
                            .content(mapper.writeValueAsString(itemRequestCreateDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemRequestService, never()).create(any(), any());
        }
    }

    @Nested
    class GetByIdItem {
        @Test
        void shouldGet() throws Exception {
            when(itemRequestService.getById(eq(user2.getId()), eq(itemRequestExtendedDto1.getId())))
                    .thenReturn(itemRequestExtendedDto1);
            mvc.perform(get("/requests/{id}", itemRequestExtendedDto1.getId())
                            .header(headerUserId, user2.getId())
                            .content(mapper.writeValueAsString(itemRequestExtendedDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemRequestExtendedDto1)));
            verify(itemRequestService, times(1))
                    .getById(eq(user2.getId()), eq(itemRequestExtendedDto1.getId()));
        }
    }

    @Nested
    class GetByRequesterIdItem {
        @Test
        void shouldGet() throws Exception {
            when(itemRequestService.getByRequesterId(eq(user2.getId())))
                    .thenReturn(List.of(itemRequestExtendedDto1, itemRequestExtendedDto2));
            mvc.perform(get("/requests")
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(
                            List.of(itemRequestExtendedDto1, itemRequestExtendedDto2))));
            verify(itemRequestService, times(1))
                    .getByRequesterId(eq(user2.getId()));
        }
    }

    @Nested
    class GetAllItem {
        @Test
        void shouldGet() throws Exception {
            when(itemRequestService.getAll(eq(user1.getId()),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(itemRequestExtendedDto1, itemRequestExtendedDto2));
            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(
                            List.of(itemRequestExtendedDto1, itemRequestExtendedDto2))));
            verify(itemRequestService, times(1)).getAll(eq(user1.getId()),
                    eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldThrowExceptionIfInvalidFrom() throws Exception {
            from = -1;
            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(itemRequestService, never()).getAll(any(), any());
        }

        @Test
        void shouldThrowExceptionIfSizeIsNegative() throws Exception {
            size = -1;
            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(itemRequestService, never()).getAll(any(), any());
        }

        @Test
        void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;
            mvc.perform(get("/requests/all?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(itemRequestService, never()).getAll(any(), any());
        }
    }
}