package ru.practicum.shareit.item;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.booking.Status.APPROVED;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemMapperImpl itemMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
    private final int from = Integer.parseInt(UserController.PAGE_DEFAULT_FROM);
    private final int size = Integer.parseInt(UserController.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
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
    private final Item item1 = Item.builder()
            .id(1L)
            .name("Item 1")
            .description("SeArCh 1 description ")
            .available(true)
            .owner(user1)
            .build();
    private final Item item2 = Item.builder()
            .id(2L)
            .name("Item 2")
            .description("SeArCh 1 description")
            .available(true)
            .owner(user2)
            .build();
    private final Item item3 = Item.builder()
            .id(3L)
            .name("Item 3")
            .description("ItEm 3 description")
            .available(false)
            .owner(user1)
            .build();
    private final ItemDto item1DtoToPatch = ItemDto.builder()
            .id(1L)
            .name("Update Item 1")
            .description("Update seaRch1 description")
            .available(false)
            .build();
    private final ItemDto item1DtoToPatchBlank = ItemDto.builder()
            .id(1L)
            .name(" ")
            .description(" ")
            .available(null)
            .build();
    private final Booking booking1 = Booking.builder()
            .id(1L)
            .start(dateTime.minusYears(10))
            .end(dateTime.minusYears(9))
            .item(item1)
            .booker(user2)
            .status(APPROVED)
            .build();
    private final Booking booking2 = Booking.builder()
            .id(2L)
            .start(dateTime.minusYears(5))
            .end(dateTime.plusYears(5))
            .item(item1)
            .booker(user2)
            .status(APPROVED)
            .build();
    private final Booking booking3 = Booking.builder()
            .id(3L)
            .start(dateTime.plusYears(8))
            .end(dateTime.plusYears(9))
            .item(item1)
            .booker(user2)
            .status(Status.WAITING)
            .build();
    private final Booking booking4 = Booking.builder()
            .id(4L)
            .start(dateTime.plusYears(9))
            .end(dateTime.plusYears(10))
            .item(item1)
            .booker(user2)
            .status(Status.REJECTED)
            .build();
    private final Comment comment1 = Comment.builder()
            .id(1L)
            .text("Comment 1")
            .created(dateTime)
            .author(user2)
            .itemId(1L)
            .build();
    private final CommentRequestDto comment1RequestDto = CommentRequestDto.builder()
            .text("CommentRequestDto")
            .build();

    @Nested
    class GetByOwnerIdItem {
        @Test
        void shouldGetTwoItems() {
            when(itemRepository.findByOwnerIdOrderByIdAsc(any(), any())).thenReturn(new PageImpl<>(List.of(item1, item3)));
            when(itemMapper.toItemExtendedDto(any(), any(), any())).thenCallRealMethod();
            itemService.getByOwnerId(user1.getId(), pageable);
            verify(itemRepository, times(1)).findByOwnerIdOrderByIdAsc(any(), any());
            verify(itemMapper, times(2)).toItemExtendedDto(any(), any(), any());
        }

        @Test
        void shouldGetZeroItems() {
            when(itemRepository.findByOwnerIdOrderByIdAsc(any(), any())).thenReturn(new PageImpl<>(List.of()));
            itemService.getByOwnerId(user1.getId(), pageable);
            verify(itemRepository, times(1)).findByOwnerIdOrderByIdAsc(any(), any());
            verify(itemMapper, never()).toItemExtendedDto(any(), any(), any());
        }
    }

    @Nested
    class GetItemByIdItem {
        @Test
        void shouldGet() {
            when(itemRepository.findById(item2.getId())).thenReturn(Optional.of(item2));
            itemService.getItemById(item2.getId());
            verify(itemRepository, times(1)).findById(any());
        }

        @Test
        void shouldThrowExceptionIfItemIdNotFound() {
            when(itemRepository.findById(item2.getId())).thenReturn(Optional.empty());
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> itemService.getItemById(item2.getId()));
            assertEquals("Вещи с таким id не существует.", exception.getMessage());
            verify(itemRepository, times(1)).findById(any());
        }
    }

    @Nested
    class GetByIdItem {
        @Test
        void shouldGetByNotOwner() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(itemMapper.toItemExtendedDto(any(), any(), any())).thenCallRealMethod();
            ItemExtendedDto itemFromService = itemService.getByIdItem(user2.getId(), item1.getId());
            assertNull(itemFromService.getLastBooking());
            assertNull(itemFromService.getNextBooking());
            verify(itemRepository, times(1)).findById(any());
            verify(itemMapper, times(1)).toItemExtendedDto(any(), any(), any());
        }

        @Test
        void shouldGetByOwnerWithLastAndNextBookings() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(itemMapper.toItemExtendedDto(any(), any(), any())).thenCallRealMethod();
            when(bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                    .thenReturn(List.of(booking2, booking1));
            when(bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any()))
                    .thenReturn(List.of(booking3, booking4));
            when(itemMapper.bookingToBookingItemDto(any())).thenCallRealMethod();
            ItemExtendedDto itemFromService = itemService.getByIdItem(user1.getId(), item1.getId());
            assertNotNull(itemFromService.getLastBooking());
            assertEquals(booking2.getId(), itemFromService.getLastBooking().getId());
            assertEquals(booking2.getBooker().getId(), itemFromService.getLastBooking().getBookerId());
            assertEquals(booking2.getStart(), itemFromService.getLastBooking().getStart());
            assertEquals(booking2.getEnd(), itemFromService.getLastBooking().getEnd());
            assertNotNull(itemFromService.getNextBooking());
            assertEquals(booking3.getId(), itemFromService.getNextBooking().getId());
            assertEquals(booking3.getBooker().getId(), itemFromService.getNextBooking().getBookerId());
            assertEquals(booking3.getStart(), itemFromService.getNextBooking().getStart());
            assertEquals(booking3.getEnd(), itemFromService.getNextBooking().getEnd());
            verify(itemRepository, times(1)).findById(any());
            verify(itemMapper, times(1)).toItemExtendedDto(any(), any(), any());
            verify(bookingRepository, times(1))
                    .findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingRepository, times(1))
                    .findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any());
            verify(itemMapper, times(2)).bookingToBookingItemDto(any());
        }

        @Test
        void shouldGetByOwnerWithEmptyLastAndNextBookings() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            when(itemMapper.toItemExtendedDto(any(), any(), any())).thenCallRealMethod();
            when(bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any()))
                    .thenReturn(List.of());
            when(bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any()))
                    .thenReturn(List.of());
            ItemExtendedDto itemFromService = itemService.getByIdItem(user1.getId(), item1.getId());
            assertNull(itemFromService.getLastBooking());
            assertNull(itemFromService.getNextBooking());
            verify(itemRepository, times(1)).findById(any());
            verify(itemMapper, times(1)).toItemExtendedDto(any(), any(), any());
            verify(bookingRepository, times(1))
                    .findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(any(), any(), any());
            verify(bookingRepository, times(1))
                    .findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(any(), any(), any());
            verify(itemMapper, never()).bookingToBookingItemDto(any());
        }
    }

    @Nested
    class CreateItem {
        @Test
        void shouldCreate() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemMapper.toItem(any(), any())).thenCallRealMethod();
            itemService.createItem(user1.getId(), itemMapper.toItemDto(item1));
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRepository, times(1)).save(item1);
            verify(itemMapper, times(2)).toItemDto(any());
            verify(itemMapper, times(1)).toItem(any(), any());
        }
    }

    @Nested
    class UpdateItem {
        @Test
        void shouldPatchByOwner() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            itemService.updateItem(user1.getId(), item1.getId(), item1DtoToPatch);
            verify(itemRepository, times(1)).findById(any());
            verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
            Item savedItem = itemArgumentCaptor.getValue();
            assertEquals(item1.getId(), savedItem.getId());
            assertEquals(item1DtoToPatch.getName(), savedItem.getName());
            assertEquals(item1DtoToPatch.getDescription(), savedItem.getDescription());
            assertEquals(item1DtoToPatch.getAvailable(), savedItem.getAvailable());
        }

        @Test
        void shouldNotPatchIfBlank() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            itemService.updateItem(user1.getId(), item1.getId(), item1DtoToPatchBlank);
            verify(itemRepository, times(1)).findById(any());
            verify(itemRepository, times(1)).save(itemArgumentCaptor.capture());
            Item savedItem = itemArgumentCaptor.getValue();
            assertEquals(item1.getId(), savedItem.getId());
            assertEquals(item1.getName(), savedItem.getName());
            assertEquals(item1.getDescription(), savedItem.getDescription());
            assertEquals(item1.getAvailable(), savedItem.getAvailable());
        }

        @Test
        void shouldThrowExceptionIfPatchByNotOwner() {
            when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
            ForbiddenException exception = assertThrows(ForbiddenException.class,
                    () -> itemService.updateItem(user2.getId(), item1.getId(), item1DtoToPatch));
            assertEquals("Изменять вещь может только владелец.", exception.getMessage());
            verify(itemRepository, times(1)).findById(any());
            verify(itemRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteItem {
        @Test
        void shouldDeleteIfIdExists() {
            itemService.deleteItem(item1.getId());
            verify(itemRepository, times(1)).deleteById(item1.getId());
        }

        @Test
        void shouldDeleteIfIdNotExists() {
            itemService.deleteItem(99L);
            verify(itemRepository, times(1)).deleteById(99L);
        }
    }

    @Nested
    class SearchItem {
        @Test
        void shouldGetEmptyListIfTextIsEmpty() {
            List<ItemDto> itemsFromService = itemService.searchItem("", pageable);
            assertTrue(itemsFromService.isEmpty());
            verify(itemRepository, never()).search(any(), any());
        }

        @Test
        void shouldGetEmptyListIfTextIsBlank() {
            List<ItemDto> itemsFromService = itemService.searchItem(" ", pageable);
            assertTrue(itemsFromService.isEmpty());
            verify(itemRepository, never()).search(any(), any());
        }

        @Test
        void shouldGetIfTextNotBlank() {
            when(itemRepository.search("iTemS", pageable)).thenReturn(new PageImpl<>(List.of(item1, item2)));
            List<ItemDto> itemsFromService = itemService.searchItem("iTemS", pageable);
            assertEquals(2, itemsFromService.size());
            verify(itemRepository, times(1)).search(any(), any());
        }
    }

    @Nested
    class AddCommentItem {
        @Test
        void shouldAdd() {
            when(itemMapper.commentRequestDtoToComment(any(), any(), any(), any())).thenCallRealMethod();
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(any(), any(), any(), any()))
                    .thenReturn(List.of(booking1, booking2));
            when(commentRepository.save(any())).thenReturn(comment1);
            when(itemMapper.commentToCommentDto(any())).thenCallRealMethod();
            CommentDto commentDto = itemService.addCommentItem(user2.getId(), item1.getId(), comment1RequestDto);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(any(), any(), any(), any());
            verify(commentRepository, times(1)).save(commentArgumentCaptor.capture());
            Comment savedComment = commentArgumentCaptor.getValue();
            savedComment.setId(commentDto.getId());
            assertEquals(comment1, savedComment);
        }

        @Test
        void shouldThrowExceptionIfNotFinishedBooking() {
            when(itemMapper.commentRequestDtoToComment(any(), any(), any(), any())).thenCallRealMethod();
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(any(), any(), any(), any()))
                    .thenReturn(List.of());
            BookingException exception = assertThrows(BookingException.class,
                    () -> itemService.addCommentItem(user2.getId(), item1.getId(), comment1RequestDto));
            assertEquals("Пользователь не брал данную вещь в аренду.", exception.getMessage());
            verify(userService, times(1)).getUserById(user2.getId());
            verify(bookingRepository, times(1))
                    .findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(any(), any(), any(), any());
            verify(commentRepository, never()).save(any());
        }
    }
}