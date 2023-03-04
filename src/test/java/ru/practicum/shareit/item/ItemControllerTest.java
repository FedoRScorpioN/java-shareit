package ru.practicum.shareit.item;

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
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.user.UserController.headerUserId;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    @MockBean
    private ItemService itemService;
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
    private final ItemDto itemDto1 = ItemDto.builder()
            .id(1L)
            .name("Item 1")
            .description("Test item 1 description 1")
            .available(true)
            .ownerId(userDto1.getId())
            .requestId(null)
            .build();
    private final ItemDto itemDto2 = ItemDto.builder()
            .id(2L)
            .name("Test Item 2")
            .description("Test Item 2 description 2")
            .available(true)
            .ownerId(userDto2.getId())
            .requestId(null)
            .build();
    private final BookingItemDto bookingItemDto1 = BookingItemDto.builder()
            .id(1L)
            .bookerId(userDto2.getId())
            .start(LocalDateTime.now().minusMinutes(10))
            .end(LocalDateTime.now().minusMinutes(5))
            .build();
    private final BookingItemDto bookingItemDto2 = BookingItemDto.builder()
            .id(2L)
            .bookerId(userDto2.getId())
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusMinutes(10))
            .build();
    private final CommentDto commentDto1 = CommentDto.builder()
            .id(1L)
            .text("Comment 1")
            .created(LocalDateTime.now().minusMinutes(10))
            .authorName(userDto2.getName())
            .build();
    private final CommentDto commentDto2 = CommentDto.builder()
            .id(2L)
            .text("Comment 2")
            .created(LocalDateTime.now().minusMinutes(5))
            .authorName(userDto2.getName())
            .build();
    private final ItemExtendedDto itemExtendedDto1 = ItemExtendedDto.builder()
            .id(itemDto1.getId())
            .name(itemDto1.getName())
            .description(itemDto1.getDescription())
            .available(itemDto1.getAvailable())
            .ownerId(itemDto1.getOwnerId())
            .requestId(null)
            .lastBooking(bookingItemDto1)
            .nextBooking(bookingItemDto2)
            .comments(List.of(commentDto1, commentDto2))
            .build();
    private final ItemExtendedDto itemExtendedDto2 = ItemExtendedDto.builder()
            .id(itemDto2.getId())
            .name(itemDto2.getName())
            .description(itemDto2.getDescription())
            .available(itemDto2.getAvailable())
            .ownerId(itemDto2.getOwnerId())
            .requestId(null)
            .lastBooking(null)
            .nextBooking(null)
            .comments(List.of())
            .build();
    private final String text = "search";
    private ItemDto itemDto;
    private CommentRequestDto commentRequestDto;
    private int from;
    private int size;

    @BeforeEach
    void beforeEach() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Test Item 1")
                .description("Test Item 1 description")
                .available(true)
                .ownerId(userDto1.getId())
                .requestId(null)
                .build();
        commentRequestDto = CommentRequestDto.builder()
                .text("Comment 1")
                .build();
        from = Integer.parseInt(UserController.PAGE_DEFAULT_FROM);
        size = Integer.parseInt(UserController.PAGE_DEFAULT_SIZE);
    }

    @Nested
    class CreateItem {
        @Test
        void shouldCreate() throws Exception {
            when(itemService.createItem(eq(userDto1.getId()), any(ItemDto.class)))
                    .thenReturn(itemDto);
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemDto)));
            verify(itemService, times(1)).createItem(eq(userDto1.getId()),
                    any(ItemDto.class));
        }

        @Test
        void shouldThrowExceptionIfNameIsNull() throws Exception {
            itemDto.setName(null);
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfNameIsEmpty() throws Exception {
            itemDto.setName("");
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfNameIsBlank() throws Exception {
            itemDto.setName(" ");
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfDescriptionIsNull() throws Exception {
            itemDto.setDescription(null);
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfDescriptionIsEmpty() throws Exception {
            itemDto.setDescription("");
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfDescriptionIsBlank() throws Exception {
            itemDto.setDescription(" ");
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfAvailableIsNull() throws Exception {
            itemDto.setAvailable(null);
            mvc.perform(post("/items")
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).createItem(any(), any());
        }
    }

    @Nested
    class GetByOwnerItem {
        @Test
        public void shouldGet() throws Exception {
            when(itemService.getByOwnerId(eq(userDto1.getId()),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(itemExtendedDto1, itemExtendedDto2));
            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(headerUserId, userDto1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemExtendedDto1, itemExtendedDto2))));
            verify(itemService, times(1)).getByOwnerId(eq(userDto1.getId()),
                    eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldThrowExceptionIfFromIsNegative() throws Exception {
            from = -1;
            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(itemService, never()).getByOwnerId(any(), any());
        }

        @Test
        void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;
            mvc.perform(get("/items?from={from}&size={size}", from, size)
                            .header(headerUserId, userDto1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(itemService, never()).getByOwnerId(any(), any());
        }
    }

    @Nested
    class GetByIdItem {
        @Test
        void shouldGet() throws Exception {
            when(itemService.getByIdItem(eq(userDto1.getId()), eq(itemDto1.getId())))
                    .thenReturn(itemExtendedDto1);
            mvc.perform(get("/items/{id}", itemDto1.getId())
                            .header(headerUserId, userDto1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemExtendedDto1)));
            verify(itemService, times(1)).getByIdItem(eq(userDto1.getId()),
                    eq(itemDto1.getId()));
        }
    }

    @Nested
    class UpdateItem {
        @Test
        void shouldPatch() throws Exception {
            when(itemService.updateItem(eq(userDto1.getId()), eq(itemDto1.getId()),
                    any(ItemDto.class)))
                    .thenReturn(itemDto1);
            mvc.perform(patch("/items/{id}", itemDto1.getId())
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(itemDto1))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(itemDto1)));
            verify(itemService, times(1)).updateItem(eq(userDto1.getId()),
                    eq(itemDto1.getId()), any(ItemDto.class));
        }
    }

    @Nested
    class DeleteItem {
        @Test
        void shouldDelete() throws Exception {
            mvc.perform(delete("/items/{id}", itemDto1.getId()))
                    .andExpect(status().isOk());
            verify(itemService, times(1)).deleteItem(eq(itemDto1.getId()));
        }
    }

    @Nested
    class SearchItem {
        @Test
        public void shouldSearch() throws Exception {
            when(itemService.searchItem(eq(text),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(itemDto1, itemDto2));
            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(itemDto1, itemDto2))));
            verify(itemService, times(1)).searchItem(eq(text),
                    eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldThrowExceptionIfFromIsNegative() throws Exception {
            from = -1;
            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isInternalServerError());
            verify(itemService, never()).searchItem(any(), any());
        }

        @Test
        void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;
            mvc.perform(get("/items/search?text={text}&from={from}&size={size}", text, from, size))
                    .andExpect(status().isInternalServerError());
            verify(itemService, never()).searchItem(any(), any());
        }
    }

    @Nested
    class AddCommentItem {
        @Test
        void shouldAdd() throws Exception {
            when(itemService.addCommentItem(eq(userDto1.getId()), eq(itemDto1.getId()),
                    any(CommentRequestDto.class)))
                    .thenReturn(commentDto1);
            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(commentDto1)));
            verify(itemService, times(1)).addCommentItem(eq(userDto1.getId()),
                    eq(itemDto1.getId()), any(CommentRequestDto.class));
        }

        @Test
        void shouldThrowExceptionIfNull() throws Exception {
            commentRequestDto.setText(null);
            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).addCommentItem(any(), any(),
                    any());
        }

        @Test
        void shouldThrowExceptionIfEmpty() throws Exception {
            commentRequestDto.setText("");
            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).addCommentItem(any(), any(),
                    any());
        }

        @Test
        void shouldThrowExceptionIfBlank() throws Exception {
            commentRequestDto.setText(" ");
            mvc.perform(post("/items/{id}/comment", itemDto1.getId())
                            .header(headerUserId, userDto1.getId())
                            .content(mapper.writeValueAsString(commentRequestDto))
                            .characterEncoding(UTF_8)
                            .contentType(APPLICATION_JSON)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(itemService, never()).addCommentItem(any(), any(),
                    any());
        }
    }
}