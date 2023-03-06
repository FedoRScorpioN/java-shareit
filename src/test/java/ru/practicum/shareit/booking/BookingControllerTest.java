package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.booking.State.ALL;
import static ru.practicum.shareit.booking.Status.WAITING;
import static ru.practicum.shareit.user.UserController.*;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    private final ObjectMapper mapper;
    private final MockMvc mvc;
    @MockBean
    private BookingService bookingService;
    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private int from;
    private int size;
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
    private final UserDto userDto2 = UserDto.builder()
            .id(user2.getId())
            .name(user2.getName())
            .email(user2.getEmail())
            .build();
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("Item Dto 1")
            .description("Item Dto 1 description")
            .available(true)
            .ownerId(user1.getId())
            .requestId(1L)
            .build();
    private final BookingResponseDto bookingResponseDto1 = BookingResponseDto.builder()
            .id(1L)
            .start(LocalDateTime.now().plusMinutes(5))
            .end(LocalDateTime.now().plusMinutes(10))
            .item(itemDto)
            .booker(userDto2)
            .status(WAITING)
            .build();
    private final BookingResponseDto bookingResponseDto2 = BookingResponseDto.builder()
            .id(2L)
            .start(LocalDateTime.now().plusMinutes(15))
            .end(LocalDateTime.now().plusMinutes(20))
            .item(itemDto)
            .booker(userDto2)
            .status(WAITING)
            .build();

    @BeforeEach
    void beforeEach() {
        bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusMinutes(5))
                .end(LocalDateTime.now().plusMinutes(10))
                .itemId(1L)
                .build();
        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusMinutes(5))
                .end(LocalDateTime.now().plusMinutes(10))
                .item(itemDto)
                .booker(userDto2)
                .status(WAITING)
                .build();
        from = Integer.parseInt(PAGE_DEFAULT_FROM);
        size = Integer.parseInt(PAGE_DEFAULT_SIZE);
    }

    @Nested
    class CreateBooking {
        @Test
        void shouldCreate() throws Exception {
            when(bookingService.createBooking(eq(user2.getId()), any(BookingRequestDto.class)))
                    .thenReturn(bookingResponseDto1);
            mvc.perform(post("/bookings")
                            .header(headerUserId, user2.getId())
                            .content(mapper.writeValueAsString(bookingRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto1)));

            verify(bookingService, times(1)).createBooking(eq(user2.getId()),
                    any(BookingRequestDto.class));
        }

        @Test
        void shouldThrowExceptionIfStartInPast() throws Exception {
            bookingRequestDto.setStart(LocalDateTime.now().minusMinutes(5));
            bookingRequestDto.setEnd(LocalDateTime.now().plusMinutes(10));
            mvc.perform(post("/bookings")
                            .header(headerUserId, user2.getId())
                            .content(mapper.writeValueAsString(bookingRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(bookingService, never()).createBooking(any(), any());
        }

        @Test
        void shouldThrowExceptionIfEndInPresent() throws Exception {
            bookingRequestDto.setStart(LocalDateTime.now().plusMinutes(5));
            bookingRequestDto.setEnd(LocalDateTime.now());
            mvc.perform(post("/bookings")
                            .header(headerUserId, user2.getId())
                            .content(mapper.writeValueAsString(bookingRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(bookingService, never()).createBooking(any(), any());
        }

        @Test
        void shouldThrowExceptionIfItemIdIsNull() throws Exception {
            bookingRequestDto.setItemId(null);
            mvc.perform(post("/bookings")
                            .header(headerUserId, user2.getId())
                            .content(mapper.writeValueAsString(bookingRequestDto))
                            .characterEncoding(StandardCharsets.UTF_8)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            verify(bookingService, never()).createBooking(any(), any());
        }
    }

    @Nested
    class UpdateBooking {
        @Test
        void shouldApproved() throws Exception {
            bookingResponseDto.setStatus(Status.APPROVED);
            when(bookingService.updateBooking(eq(user2.getId()), eq(bookingResponseDto.getId()),
                    eq(true)))
                    .thenReturn(bookingResponseDto);
            mvc.perform(patch("/bookings/{id}?approved={approved}", bookingResponseDto.getId(), true)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));
            verify(bookingService, times(1)).updateBooking(eq(user2.getId()),
                    eq(bookingResponseDto.getId()), eq(true));
        }

        @Test
        void shouldReject() throws Exception {
            bookingResponseDto.setStatus(Status.REJECTED);
            when(bookingService.updateBooking(eq(user2.getId()), eq(bookingResponseDto.getId()),
                    eq(false)))
                    .thenReturn(bookingResponseDto);
            mvc.perform(patch("/bookings/{id}?approved={approved}", bookingResponseDto.getId(), false)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto)));

            verify(bookingService, times(1)).updateBooking(eq(user2.getId()),
                    eq(bookingResponseDto.getId()), eq(false));
        }
    }

    @Nested
    class GetByIdBooking {
        @Test
        void shouldGet() throws Exception {
            when(bookingService.getByIdBooking(eq(user2.getId()), eq(bookingResponseDto1.getId())))
                    .thenReturn(bookingResponseDto1);
            mvc.perform(get("/bookings/{id}", bookingResponseDto1.getId())
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(bookingResponseDto1)));
            verify(bookingService, times(1))
                    .getByIdBooking(eq(user2.getId()), eq(bookingResponseDto1.getId()));
        }
    }

    @Nested
    class GetAllByByBookerId {
        @Test
        void shouldGetWithValidState() throws Exception {
            when(bookingService.getAllByBookerId(eq(userDto2.getId()), eq(ALL),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));
            mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "All", from, size)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                    .getAllByBookerId(eq(userDto2.getId()), eq(ALL),
                            eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldGetWithDefaultState() throws Exception {
            when(bookingService.getAllByBookerId(eq(userDto2.getId()), eq(ALL),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));
            mvc.perform(get("/bookings?from={from}&size={size}", from, size)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));

            verify(bookingService, times(1))
                    .getAllByBookerId(eq(userDto2.getId()), eq(ALL),
                            eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldThrowExceptionIfUnknownState() throws Exception {
            mvc.perform(get("/bookings?state={state}&from={from}&size={size}", "unknown", from, size)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isInternalServerError());

            verify(bookingService, never())
                    .getAllByBookerId(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionIfFromIsNegative() throws Exception {
            from = -1;

            mvc.perform(get("/bookings?from={from}&size={size}", from, size)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isInternalServerError());

            verify(bookingService, never())
                    .getAllByBookerId(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;
            mvc.perform(get("/bookings?from={from}&size={size}", from, size)
                            .header(headerUserId, user2.getId()))
                    .andExpect(status().isInternalServerError());
            verify(bookingService, never())
                    .getAllByBookerId(any(), any(), any());
        }
    }

    @Nested
    class GetAllByByOwnerId {
        @Test
        void shouldGetWithValidState() throws Exception {
            when(bookingService.getAllByOwnerId(eq(itemDto.getOwnerId()), eq(ALL),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));
            mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "All", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));
            verify(bookingService, times(1))
                    .getAllByOwnerId(eq(itemDto.getOwnerId()), eq(ALL),
                            eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldGetWithDefaultState() throws Exception {
            when(bookingService.getAllByOwnerId(eq(itemDto.getOwnerId()), eq(ALL),
                    eq(PageRequest.of(from / size, size))))
                    .thenReturn(List.of(bookingResponseDto1, bookingResponseDto2));
            mvc.perform(get("/bookings/owner?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(mapper.writeValueAsString(List.of(bookingResponseDto1, bookingResponseDto2))));
            verify(bookingService, times(1))
                    .getAllByOwnerId(eq(itemDto.getOwnerId()), eq(ALL),
                            eq(PageRequest.of(from / size, size)));
        }

        @Test
        void shouldThrowExceptionIfUnknownState() throws Exception {
            mvc.perform(get("/bookings/owner?state={state}&from={from}&size={size}", "unknown", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(bookingService, never())
                    .getAllByOwnerId(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionIfFromIsNegative() throws Exception {
            from = -1;
            mvc.perform(get("/bookings/owner?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(bookingService, never())
                    .getAllByOwnerId(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionIfSizeIsZero() throws Exception {
            size = 0;
            mvc.perform(get("/bookings/owner?from={from}&size={size}", from, size)
                            .header(headerUserId, user1.getId()))
                    .andExpect(status().isInternalServerError());
            verify(bookingService, never())
                    .getAllByOwnerId(any(), any(), any());
        }
    }
}