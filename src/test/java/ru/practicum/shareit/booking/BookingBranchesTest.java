package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.practicum.shareit.user.UserController.PAGE_DEFAULT_FROM;
import static ru.practicum.shareit.user.UserController.PAGE_DEFAULT_SIZE;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingBranchesTest {
    private final UserController userController;
    private final ItemController itemController;
    private final BookingController bookingController;
    private final BookingService bookingService;

    private void checkBookingResponseDto(BookingResponseDto bookingResponseDto, BookingRequestDto bookingRequestDto,
                                         ItemDto itemDto, UserDto userDto) {
        assertEquals(bookingResponseDto.getStatus(), Status.WAITING);
        assertEquals(bookingResponseDto.getStart(), bookingRequestDto.getStart());
        assertEquals(bookingResponseDto.getEnd(), bookingRequestDto.getEnd());
        assertEquals(bookingResponseDto.getItem().getId(), itemDto.getId());
        assertEquals(bookingResponseDto.getItem().getName(), itemDto.getName());
        assertEquals(bookingResponseDto.getItem().getDescription(), itemDto.getDescription());
        assertEquals(bookingResponseDto.getItem().getAvailable(), itemDto.getAvailable());
        assertEquals(bookingResponseDto.getItem().getOwnerId(), itemDto.getOwnerId());
        assertEquals(bookingResponseDto.getBooker().getId(), userDto.getId());
        assertEquals(bookingResponseDto.getBooker().getName(), userDto.getName());
        assertEquals(bookingResponseDto.getBooker().getEmail(), userDto.getEmail());
    }

    @Nested
    class CreateBooking {
        @Test
        void shouldCreate() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test Item 1")
                    .description("Test Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingFromService = bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            assertEquals(bookingFromService.getId(), 1L);
            checkBookingResponseDto(bookingFromService, bookingRequestDto, itemDto, userDto2);
        }

        @Test
        public void shouldThrowExceptionIfUserIdNotFound() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(false)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingException exception = assertThrows(BookingException.class,
                    () -> bookingService.createBooking(100L, bookingRequestDto));
            assertEquals("Предмет недоступен для бронирования.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfItemIdNotFound() {
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
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(100L)
                    .build();
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingService.createBooking(userDto2.getId(), bookingRequestDto));
            assertEquals("Вещи с таким id не существует.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfItemNotAvailable() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test Item 1")
                    .description("Test Item description 1")
                    .available(false)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingException exception = assertThrows(BookingException.class,
                    () -> bookingService.createBooking(userDto2.getId(), bookingRequestDto));
            assertEquals("Предмет недоступен для бронирования.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfBookingByOwner() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test Item 1")
                    .description("Test Item description 1")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingService.createBooking(userDto.getId(), bookingRequestDto));
            assertEquals("Владелец не может бронировать собственную вещь.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfEndIsAfterStart() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 3, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingException exception = assertThrows(BookingException.class,
                    () -> bookingService.createBooking(userDto2.getId(), bookingRequestDto));
            assertEquals("Недопустимое время брони.", exception.getMessage());
        }
    }

    @Nested
    class Patch {
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
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto1.getOwnerId(), itemDto1);
            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Item 2")
                    .description("Item description 2")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto2.getOwnerId(), itemDto2);
            BookingRequestDto bookingRequestDto1 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto1 = bookingService.createBooking(userDto2.getId(), bookingRequestDto1);
            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto2.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.createBooking(userDto2.getId(), bookingRequestDto2);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto1.getId(), true);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto2.getId(), false);
            BookingResponseDto booking1 = bookingController.getByIdBooking(userDto1.getId(), bookingResponseDto1.getId());
            BookingResponseDto booking2 = bookingController.getByIdBooking(userDto1.getId(), bookingResponseDto2.getId());
            assertEquals(booking1.getId(), 1L);
            assertEquals(booking1.getStatus(), Status.APPROVED);
            assertEquals(booking2.getId(), 2L);
            assertEquals(booking2.getStatus(), Status.REJECTED);
        }

        @Test
        void shouldThrowExceptionIfUserIdNotFound() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.updateBooking(100L, bookingResponseDto.getId(), true));
            assertEquals("Изменение статуса бронирования доступно только владельцу.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfItemIdNotFound() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.updateBooking(userDto1.getId(), 100L, true));
            assertEquals("Бронирование с таким id не существует.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfPatchNotOwner() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto1 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            bookingService.createBooking(userDto2.getId(), bookingRequestDto1);
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.updateBooking(userDto2.getId(), itemDto.getId(), true));
            assertEquals("Изменение статуса бронирования доступно только владельцу.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfPatchTwice() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto1 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            bookingService.createBooking(userDto2.getId(), bookingRequestDto1);
            bookingController.updateBooking(userDto1.getId(), itemDto.getId(), true);
            BookingException exception = assertThrows(BookingException.class,
                    () -> bookingController.updateBooking(userDto1.getId(), itemDto.getId(), true));
            assertEquals("Ответ по бронированию уже дан.", exception.getMessage());
        }
    }

    @Nested
    class GetByIdBooking {
        @Test
        void shouldGetByAuthorOrOwner() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            BookingResponseDto bookingGetByController1 = bookingController.getByIdBooking(userDto2.getId(), bookingResponseDto.getId());
            BookingResponseDto bookingGetByController2 = bookingController.getByIdBooking(userDto1.getId(), bookingResponseDto.getId());
            assertEquals(bookingGetByController1.getId(), 1L);
            assertEquals(bookingGetByController2.getId(), 1L);
            checkBookingResponseDto(bookingGetByController1, bookingRequestDto, itemDto, userDto2);
            checkBookingResponseDto(bookingGetByController2, bookingRequestDto, itemDto, userDto2);
        }

        @Test
        void shouldThrowExceptionIfBookingNotFound() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto);
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.getByIdBooking(userDto.getId(), 100L));
            assertEquals("Бронирование с таким id не существует.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfRequestByNotAuthorOrOwner() {
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
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.getByIdBooking(100L, bookingResponseDto.getId()));
            assertEquals("Просмотр бронирования доступно только автору или владельцу.", exception.getMessage());
        }
    }

    @Nested
    class GetAllByByBookerId {
        @BeforeEach
        void addBookings() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("Test user 1")
                    .email("tester1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("Test user 2")
                    .email("tester2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item 1")
                    .description("Test item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            BookingRequestDto bookingRequestDtoPast = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 29, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 29, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDtoPast = bookingService.createBooking(userDto2.getId(), bookingRequestDtoPast);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDtoPast.getId(), true);
            BookingRequestDto bookingRequestDtoCurrent = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 7, 30, 10, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDtoCurrent = bookingService.createBooking(userDto2.getId(), bookingRequestDtoCurrent);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDtoCurrent.getId(), true);
            BookingRequestDto bookingRequestDtoFuture = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 7, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 7, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            bookingService.createBooking(userDto2.getId(), bookingRequestDtoFuture);
            BookingRequestDto bookingRequestDtoRejected = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 8, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 8, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDtoRejected = bookingService.createBooking(userDto2.getId(), bookingRequestDtoRejected);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDtoRejected.getId(), false);
        }

        @Test
        void shouldGetByStateAll() {
            List<BookingResponseDto> bookings = bookingController.getAllByBookerId(2L,
                    "ALL",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 4);
            assertEquals(bookings.get(0).getId(), 4L);
            assertEquals(bookings.get(1).getId(), 3L);
            assertEquals(bookings.get(2).getId(), 2L);
            assertEquals(bookings.get(3).getId(), 1L);
        }

        @Test
        void shouldGetByStateCurrent() {
            List<BookingResponseDto> bookings = bookingController.getAllByBookerId(2L,
                    "CURRENT",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));

            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getId(), 2L);
        }

        @Test
        void shouldGetByStatePast() {
            List<BookingResponseDto> bookings = bookingController.getAllByBookerId(2L,
                    "PAST",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));

            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getId(), 1L);
        }

        @Test
        void shouldGetByStateFuture() {
            List<BookingResponseDto> bookings = bookingController.getAllByBookerId(2L,
                    "FUTURE",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 2);
            assertEquals(bookings.get(0).getId(), 4L);
            assertEquals(bookings.get(1).getId(), 3L);
        }

        @Test
        void shouldGetByStateWaiting() {
            List<BookingResponseDto> bookings = bookingController.getAllByBookerId(2L,
                    "WAITING",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getId(), 3L);
        }

        @Test
        void shouldGetByStateRejected() {
            List<BookingResponseDto> bookings = bookingController.getAllByBookerId(2L,
                    "REJECTED",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getId(), 4L);
        }

        @Test
        void shouldThrowExceptionIfBookerNotFound() {
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.getAllByBookerId(100L,
                            "ALL",
                            Integer.parseInt(PAGE_DEFAULT_FROM),
                            Integer.parseInt(PAGE_DEFAULT_SIZE)));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionWithUnknownState() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingController.getAllByBookerId(2L,
                            "UNKNOWN",
                            Integer.parseInt(PAGE_DEFAULT_FROM),
                            Integer.parseInt(PAGE_DEFAULT_SIZE)));
            assertEquals("Unknown state: UNKNOWN", exception.getMessage());
        }
    }

    @Nested
    class GetAllByByOwnerId {
        @BeforeEach
        void addBookings() {
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
            ItemDto itemDtoPast = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDtoPast.getOwnerId(), itemDtoPast);
            BookingRequestDto bookingRequestDtoPast = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 29, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 29, 11, 0, 0))
                    .itemId(itemDtoPast.getId())
                    .build();
            BookingResponseDto bookingResponseDtoPast = bookingService.createBooking(userDto2.getId(), bookingRequestDtoPast);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDtoPast.getId(), true);
            ItemDto itemDtoCurrent = ItemDto.builder()
                    .id(2L)
                    .name("Test item 2")
                    .description("Test item description 2")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDtoCurrent.getOwnerId(), itemDtoCurrent);
            BookingRequestDto bookingRequestDtoCurrent = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 7, 30, 10, 0, 0))
                    .itemId(itemDtoCurrent.getId())
                    .build();
            BookingResponseDto bookingResponseDtoCurrent = bookingService.createBooking(userDto2.getId(), bookingRequestDtoCurrent);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDtoCurrent.getId(), true);
            ItemDto itemDtoFuture = ItemDto.builder()
                    .id(3L)
                    .name("Item 3")
                    .description("Item description 3")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDtoFuture.getOwnerId(), itemDtoFuture);
            BookingRequestDto bookingRequestDtoFuture = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 7, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 7, 30, 11, 0, 0))
                    .itemId(itemDtoFuture.getId())
                    .build();
            bookingService.createBooking(userDto2.getId(), bookingRequestDtoFuture);
            ItemDto itemDtoRejected = ItemDto.builder()
                    .id(4L)
                    .name("Item 4")
                    .description("Item description 4")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDtoRejected.getOwnerId(), itemDtoRejected);
            BookingRequestDto bookingRequestDtoRejected = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 8, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 8, 30, 11, 0, 0))
                    .itemId(itemDtoRejected.getId())
                    .build();
            BookingResponseDto bookingResponseDtoRejected = bookingService.createBooking(userDto2.getId(), bookingRequestDtoRejected);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDtoRejected.getId(), false);
        }

        @Test
        void shouldGetByStateAll() {
            List<BookingResponseDto> bookings = bookingController.getAllByOwnerId(1L,
                    "ALL",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 4);
            assertEquals(bookings.get(0).getItem().getId(), 4L);
            assertEquals(bookings.get(1).getItem().getId(), 3L);
            assertEquals(bookings.get(2).getItem().getId(), 2L);
            assertEquals(bookings.get(3).getItem().getId(), 1L);
        }

        @Test
        void shouldGetByStateCurrent() {
            List<BookingResponseDto> bookings = bookingController.getAllByOwnerId(1L,
                    "CURRENT",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getItem().getId(), 2L);
        }

        @Test
        void shouldGetByStatePast() {
            List<BookingResponseDto> bookings = bookingController.getAllByOwnerId(1L,
                    "PAST",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getItem().getId(), 1L);
        }

        @Test
        void shouldGetByStateFuture() {
            List<BookingResponseDto> bookings = bookingController.getAllByOwnerId(1L,
                    "FUTURE",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 2);
            assertEquals(bookings.get(0).getItem().getId(), 4L);
            assertEquals(bookings.get(1).getItem().getId(), 3L);
        }

        @Test
        void shouldGetByStateWaiting() {
            List<BookingResponseDto> bookings = bookingController.getAllByOwnerId(1L,
                    "WAITING",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));

            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getItem().getId(), 3L);
        }

        @Test
        void shouldGetByStateRejected() {
            List<BookingResponseDto> bookings = bookingController.getAllByOwnerId(1L,
                    "REJECTED",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(bookings.size(), 1);
            assertEquals(bookings.get(0).getItem().getId(), 4L);
        }

        @Test
        void shouldThrowExceptionIfOwnerNotFound() {
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> bookingController.getAllByOwnerId(100L,
                            "ALL",
                            Integer.parseInt(PAGE_DEFAULT_FROM),
                            Integer.parseInt(PAGE_DEFAULT_SIZE)));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
        }

        @Test
        public void shouldThrowExceptionWithUnknownState() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingController.getAllByOwnerId(1L,
                            "UNKNOWN",
                            Integer.parseInt(PAGE_DEFAULT_FROM),
                            Integer.parseInt(PAGE_DEFAULT_SIZE)));
            assertEquals("Unknown state: UNKNOWN", exception.getMessage());
        }
    }
}