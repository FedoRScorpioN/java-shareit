package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.user.UserController.PAGE_DEFAULT_FROM;
import static ru.practicum.shareit.user.UserController.PAGE_DEFAULT_SIZE;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemBranchesTest {
    private final UserController userController;
    private final ItemController itemController;
    private final BookingController bookingController;
    private final BookingService bookingService;

    void checkItemExtendedDto(ItemExtendedDto itemFromController, ItemDto itemDto) {
        assertEquals(itemFromController.getId(), itemDto.getId());
        assertEquals(itemFromController.getName(), itemDto.getName());
        assertEquals(itemFromController.getDescription(), itemDto.getDescription());
        assertEquals(itemFromController.getAvailable(), itemDto.getAvailable());
        assertEquals(itemFromController.getOwnerId(), itemDto.getOwnerId());
        assertEquals(itemFromController.getRequestId(), itemDto.getRequestId());
    }

    void checkItemExtendedDtoBooking(ItemExtendedDto itemFromController,
                                     BookingResponseDto lastBookingResponseDto,
                                     BookingResponseDto nextBookingResponseDto) {
        assertEquals(itemFromController.getLastBooking().getId(), lastBookingResponseDto.getId());
        assertEquals(itemFromController.getLastBooking().getBookerId(), lastBookingResponseDto.getBooker().getId());
        assertEquals(itemFromController.getLastBooking().getStart(), lastBookingResponseDto.getStart());
        assertEquals(itemFromController.getLastBooking().getEnd(), lastBookingResponseDto.getEnd());

        assertEquals(itemFromController.getNextBooking().getId(), nextBookingResponseDto.getId());
        assertEquals(itemFromController.getNextBooking().getBookerId(), nextBookingResponseDto.getBooker().getId());
        assertEquals(itemFromController.getNextBooking().getStart(), nextBookingResponseDto.getStart());
        assertEquals(itemFromController.getNextBooking().getEnd(), nextBookingResponseDto.getEnd());
    }

    void checkItemDto(ItemDto itemDtoFromController, ItemDto itemDto) {
        assertEquals(itemDtoFromController.getId(), itemDto.getId());
        assertEquals(itemDtoFromController.getName(), itemDto.getName());
        assertEquals(itemDtoFromController.getDescription(), itemDto.getDescription());
        assertEquals(itemDtoFromController.getAvailable(), itemDto.getAvailable());
        assertEquals(itemDtoFromController.getOwnerId(), itemDto.getOwnerId());
        assertEquals(itemDtoFromController.getRequestId(), itemDto.getRequestId());
    }

    @Nested
    class CreateItem {
        @Test
        void shouldCreate() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            List<ItemExtendedDto> itemsFromController = itemController.getByOwnerId(
                    userDto.getId(),
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(itemsFromController.size(), 1);
            ItemExtendedDto itemFromController = itemsFromController.get(0);
            checkItemExtendedDto(itemFromController, itemDto);
        }

        @Test
        void shouldThrowExceptionIfItemOwnerIdNotFound() {
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Test item")
                    .description("Test item description")
                    .available(true)
                    .ownerId(10L)
                    .requestId(null)
                    .build();
            NotFoundException exception = assertThrows(NotFoundException.class, () -> itemController.createItem(10L, itemDto));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());
        }
    }

    @Nested
    class GetByOwner {
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
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto2.getOwnerId(), itemDto2);
            ItemDto itemDto3 = ItemDto.builder()
                    .id(3L)
                    .name("Item 3")
                    .description("Item description 3")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto3.getOwnerId(), itemDto3);
            List<ItemExtendedDto> itemsFromController1 = itemController.getByOwnerId(
                    userDto1.getId(),
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(itemsFromController1.size(), 2);
            ItemExtendedDto itemFromController1 = itemsFromController1.get(0);
            ItemExtendedDto itemFromController3 = itemsFromController1.get(1);
            checkItemExtendedDto(itemFromController1, itemDto1);
            checkItemExtendedDto(itemFromController3, itemDto3);
            List<ItemExtendedDto> itemsFromController2 = itemController.getByOwnerId(
                    userDto2.getId(),
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(itemsFromController2.size(), 1);
            ItemExtendedDto itemFromController2 = itemsFromController2.get(0);
            checkItemExtendedDto(itemFromController2, itemDto2);
        }

        @Test
        void shouldGetIfEmpty() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            List<ItemExtendedDto> itemsFromController = itemController.getByOwnerId(
                    userDto.getId(),
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertTrue(itemsFromController.isEmpty());
        }

        @Test
        void shouldHaveBookingDateAndComments() {
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
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto1.getId(), true);
            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 3, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 4, 30, 11, 0, 0))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.createBooking(userDto2.getId(), bookingRequestDto2);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto2.getId(), true);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addCommentItem(userDto2.getId(), itemDto1.getId(), commentRequestDto);
            List<ItemExtendedDto> itemsFromController = itemController.getByOwnerId(
                    userDto1.getId(),
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(itemsFromController.size(), 2);
            ItemExtendedDto itemFromController1 = itemsFromController.get(0);
            ItemExtendedDto itemFromController2 = itemsFromController.get(1);
            assertEquals(itemFromController1.getId(), itemDto1.getId());
            checkItemExtendedDtoBooking(itemFromController1, bookingResponseDto1, bookingResponseDto2);
            List<CommentDto> commentsItem1 = itemFromController1.getComments();
            assertEquals(commentsItem1.size(), 1);
            CommentDto commentDto = commentsItem1.get(0);
            assertEquals(commentDto.getText(), commentRequestDto.getText());
            assertEquals(commentDto.getAuthorName(), userDto2.getName());
            assertEquals(itemFromController2.getId(), itemDto2.getId());
            assertNull(itemFromController2.getLastBooking());
            assertNull(itemFromController2.getNextBooking());
            List<CommentDto> commentsItem2 = itemFromController2.getComments();
            assertTrue(commentsItem2.isEmpty());
        }
    }

    @Nested
    class GetById {
        @Test
        void shouldGet() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            ItemExtendedDto itemFromController = itemController.getByIdItem(userDto.getId(), itemDto.getId());
            checkItemExtendedDto(itemFromController, itemDto);
        }

        @Test
        void shouldThrowExceptionIfItemIdNotFound() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            NotFoundException exception = assertThrows(NotFoundException.class, () -> itemController.getByIdItem(userDto.getId(), 10L));
            assertEquals("Вещи с таким id не существует.", exception.getMessage());
        }

        @Test
        void shouldRequestByOwnerHaveBookingDateAndComments() {
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
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto1.getId(), true);
            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 3, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 4, 30, 11, 0, 0))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.createBooking(userDto2.getId(), bookingRequestDto2);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto2.getId(), true);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addCommentItem(userDto2.getId(), itemDto1.getId(), commentRequestDto);
            ItemExtendedDto itemFromController1 = itemController.getByIdItem(userDto1.getId(), itemDto1.getId());
            assertEquals(itemFromController1.getId(), itemDto1.getId());
            checkItemExtendedDtoBooking(itemFromController1, bookingResponseDto1, bookingResponseDto2);
            List<CommentDto> commentsItem1 = itemFromController1.getComments();
            assertEquals(commentsItem1.size(), 1);
            CommentDto comment = commentsItem1.get(0);
            assertEquals(comment.getText(), commentRequestDto.getText());
            assertEquals(comment.getAuthorName(), userDto2.getName());
            ItemExtendedDto itemFromController2 = itemController.getByIdItem(userDto1.getId(), itemDto2.getId());
            assertEquals(itemFromController2.getId(), itemDto2.getId());
            assertNull(itemFromController2.getLastBooking());
            assertNull(itemFromController2.getNextBooking());
            List<CommentDto> commentsItem2 = itemFromController2.getComments();
            assertTrue(commentsItem2.isEmpty());
        }

        @Test
        void shouldRequestByNoOwnerHaveNotBookingDateAndHaveComments() {
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
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto1.getId(), true);
            BookingRequestDto bookingRequestDto2 = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 3, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 4, 30, 11, 0, 0))
                    .itemId(itemDto1.getId())
                    .build();
            BookingResponseDto bookingResponseDto2 = bookingService.createBooking(userDto2.getId(), bookingRequestDto2);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto2.getId(), true);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addCommentItem(userDto2.getId(), itemDto1.getId(), commentRequestDto);
            ItemExtendedDto itemFromController1 = itemController.getByIdItem(userDto2.getId(), itemDto1.getId());
            assertEquals(itemFromController1.getId(), itemDto1.getId());
            assertNull(itemFromController1.getLastBooking());
            assertNull(itemFromController1.getNextBooking());
            List<CommentDto> commentsItem1 = itemFromController1.getComments();
            assertEquals(commentsItem1.size(), 1);
            CommentDto comment = commentsItem1.get(0);
            assertEquals(comment.getText(), commentRequestDto.getText());
            assertEquals(comment.getAuthorName(), userDto2.getName());
            ItemExtendedDto itemFromController2 = itemController.getByIdItem(userDto2.getId(), itemDto2.getId());
            assertEquals(itemFromController2.getId(), itemDto2.getId());
            assertNull(itemFromController2.getLastBooking());
            assertNull(itemFromController2.getNextBooking());
            List<CommentDto> commentsItem2 = itemFromController2.getComments();
            assertTrue(commentsItem2.isEmpty());
        }
    }

    @Nested
    class UpdateItem {
        @Test
        void shouldPatch() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            ItemDto itemDto1 = ItemDto.builder()
                    .id(1L)
                    .name("Item 1")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto1.getOwnerId(), itemDto1);
            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Update item 1")
                    .description("Update item description 1")
                    .available(false)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.updateItem(itemDto2.getOwnerId(), itemDto1.getId(), itemDto2);
            ItemExtendedDto itemFromController = itemController.getByIdItem(userDto.getId(), itemDto1.getId());
            assertEquals(itemFromController.getId(), itemDto1.getId());
            assertEquals(itemFromController.getName(), itemDto2.getName());
            assertEquals(itemFromController.getDescription(), itemDto2.getDescription());
            assertEquals(itemFromController.getAvailable(), itemDto2.getAvailable());
            assertEquals(itemFromController.getOwnerId(), itemDto2.getOwnerId());
            assertEquals(itemFromController.getRequestId(), itemDto2.getRequestId());
        }

        @Test
        void shouldThrowExceptionIfItemOwnerIdForbidden() {
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
                    .name("Update item 1")
                    .description("Update item description 1")
                    .available(false)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            ForbiddenException exception = assertThrows(ForbiddenException.class, () -> itemController.updateItem(itemDto2.getOwnerId(), itemDto1.getId(), itemDto2));
            assertEquals("Изменять вещь может только владелец.", exception.getMessage());
            ItemExtendedDto itemFromController = itemController.getByIdItem(userDto1.getId(), itemDto1.getId());
            checkItemExtendedDto(itemFromController, itemDto1);
        }
    }

    @Nested
    class DeleteItem {
        @Test
        void shouldDelete() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(userDto.getId(), itemDto);
            itemController.deleteItem(itemDto.getId());
            assertTrue(itemController.getByOwnerId(userDto.getId(),
                            Integer.parseInt(PAGE_DEFAULT_FROM),
                            Integer.parseInt(PAGE_DEFAULT_SIZE))
                    .isEmpty());
        }

        @Test
        void shouldDeleteIfItemIdNotFound() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            assertThrows(EmptyResultDataAccessException.class, () -> itemController.deleteItem(10L));
            NotFoundException exception = assertThrows(NotFoundException.class, () -> itemController.getByIdItem(userDto.getId(), 10L));
            assertEquals("Вещи с таким id не существует.", exception.getMessage());
        }
    }

    @Nested
    class Search {
        @Test
        void shouldSearch() {
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
                    .name("Item 1 OtVeRtKa")
                    .description("Item description 1")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto1.getOwnerId(), itemDto1);
            ItemDto itemDto2 = ItemDto.builder()
                    .id(2L)
                    .name("Item 2 OtVeRtKa")
                    .description("Item description 2 OtVeRtKa")
                    .available(false)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto2.getOwnerId(), itemDto2);
            ItemDto itemDto3 = ItemDto.builder()
                    .id(3L)
                    .name("Item 3")
                    .description("Test item description 3 OtVeRtKa")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto3.getOwnerId(), itemDto3);
            ItemDto itemDto4 = ItemDto.builder()
                    .id(4L)
                    .name("Item 4")
                    .description("Item description 4")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto4.getOwnerId(), itemDto4);

            List<ItemDto> itemsFromController = itemController.searchItem(
                    "oTvErTkA",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(itemsFromController.size(), 2);
            ItemDto itemFromController1 = itemsFromController.get(0);
            ItemDto itemFromController2 = itemsFromController.get(1);
            checkItemDto(itemFromController1, itemDto1);
            checkItemDto(itemFromController2, itemDto3);
        }

        @Test
        void shouldSearchIfEmpty() {
            UserDto userDto = UserDto.builder()
                    .id(1L)
                    .name("User")
                    .email("mail@yandex.ru")
                    .build();
            userController.createUser(userDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            List<ItemDto> itemsFromController = itemController.searchItem(" ",
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertTrue(itemsFromController.isEmpty());
        }
    }

    @Nested
    class AddCommentItem {
        @Test
        void shouldCreate() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto.getId(), true);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            itemController.addCommentItem(userDto2.getId(), itemDto.getId(), commentRequestDto);
            ItemExtendedDto item = itemController.getByIdItem(userDto1.getId(), itemDto.getId());
            List<CommentDto> comments = item.getComments();
            assertEquals(comments.size(), 1);
            CommentDto comment = comments.get(0);
            assertEquals(comment.getText(), commentRequestDto.getText());
            assertEquals(comment.getAuthorName(), userDto2.getName());
        }

        @Test
        void shouldThrowExceptionIfNoBooking() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            BookingException exception = assertThrows(BookingException.class,
                    () -> itemController.addCommentItem(userDto2.getId(), itemDto.getId(), commentRequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfBookingNotFinished() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2050, 3, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            BookingResponseDto bookingResponseDto = bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            bookingController.updateBooking(userDto1.getId(), bookingResponseDto.getId(), true);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            BookingException exception = assertThrows(BookingException.class,
                    () -> itemController.addCommentItem(userDto2.getId(), itemDto.getId(), commentRequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
        }

        @Test
        void shouldThrowExceptionIfBookingNotApproved() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto1.getId())
                    .requestId(null)
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            UserDto userDto2 = UserDto.builder()
                    .id(2L)
                    .name("User 2")
                    .email("mail2@yandex.ru")
                    .build();
            userController.createUser(userDto2);
            BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                    .start(LocalDateTime.of(2023, 1, 30, 10, 0, 0))
                    .end(LocalDateTime.of(2023, 1, 30, 11, 0, 0))
                    .itemId(itemDto.getId())
                    .build();
            bookingService.createBooking(userDto2.getId(), bookingRequestDto);
            CommentRequestDto commentRequestDto = new CommentRequestDto("comment");
            BookingException exception = assertThrows(BookingException.class,
                    () -> itemController.addCommentItem(userDto2.getId(), itemDto.getId(), commentRequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
        }
    }
}