package ru.practicum.shareit.request;

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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapperImpl;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @Mock
    private UserService userService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRequestMapperImpl itemRequestMapper;
    @Mock
    private ItemMapperImpl itemMapper;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;
    private final int from = Integer.parseInt(UserController.PAGE_DEFAULT_FROM);
    private final int size = Integer.parseInt(UserController.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
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
            .description("Item 1 description")
            .available(true)
            .owner(user1)
            .requestId(1L)
            .build();
    private final ItemRequestCreateDto item1RequestCreateDto = ItemRequestCreateDto.builder()
            .description("Item 1 description")
            .build();
    private final ItemRequest itemRequest1 = ItemRequest.builder()
            .id(1L)
            .description("ItemRequest 1 description")
            .requesterId(user2)
            .created(dateTime)
            .items(List.of(item1))
            .build();

    void checkItemRequestExtendedDto(ItemRequest itemRequest1, ItemRequestExtendedDto itemRequestExtendedDto) {
        assertEquals(itemRequest1.getId(), itemRequestExtendedDto.getId());
        assertEquals(itemRequest1.getDescription(), itemRequestExtendedDto.getDescription());
        assertEquals(itemRequest1.getCreated(), itemRequestExtendedDto.getCreated());
        assertEquals(itemRequest1.getItems().size(), itemRequestExtendedDto.getItems().size());
        Item item = itemRequest1.getItems().get(0);
        ItemDto resultItemDto = itemRequestExtendedDto.getItems().get(0);
        assertEquals(item.getId(), resultItemDto.getId());
        assertEquals(item.getName(), resultItemDto.getName());
        assertEquals(item.getAvailable(), resultItemDto.getAvailable());
        assertEquals(item.getDescription(), resultItemDto.getDescription());
        assertEquals(item.getOwner().getId(), resultItemDto.getOwnerId());
    }

    @Nested
    class CreateItem {
        @Test
        void shouldCreate() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestMapper.toItemRequest(any(), any(), any())).thenCallRealMethod();
            when(itemRequestRepository.save(any())).thenReturn(itemRequest1);
            when(itemRequestMapper.toItemRequestDto(any())).thenCallRealMethod();
            ItemRequestDto result = itemRequestService.create(user2.getId(), item1RequestCreateDto);
            verify(itemRequestRepository, times(1)).save(itemRequestArgumentCaptor.capture());
            verify(itemRequestMapper, times(1)).toItemRequest(any(), any(), any());
            ItemRequest savedItemRequest = itemRequestArgumentCaptor.getValue();
            savedItemRequest.setId(result.getId());
            assertEquals(itemRequest1, savedItemRequest);
            assertEquals(item1RequestCreateDto.getDescription(), savedItemRequest.getDescription());
            assertEquals(user2.getId(), savedItemRequest.getRequesterId().getId());
            assertEquals(user2.getName(), savedItemRequest.getRequesterId().getName());
            assertEquals(user2.getEmail(), savedItemRequest.getRequesterId().getEmail());
            assertNotNull(savedItemRequest.getCreated());
            assertNull(savedItemRequest.getItems());
        }
    }

    @Nested
    class GetByIdItem {
        @Test
        void shouldGet() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest1));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestExtendedDto(any(), any())).thenCallRealMethod();
            ItemRequestExtendedDto result = itemRequestService.getById(user2.getId(), 1L);
            checkItemRequestExtendedDto(itemRequest1, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(itemRequestRepository, times(1)).findById(1L);
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestExtendedDto(any(), any());
        }

        @Test
        void shouldThrowExceptionIfItemRequestIdNotFound() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> itemRequestService.getById(user2.getId(), 1L));
            assertEquals("Запроса вещи с таким id не существует.", exception.getMessage());
            verify(userService, times(1)).getUserById(user2.getId());
            verify(itemRequestRepository, times(1)).findById(1L);
        }
    }

    @Nested
    class GetByRequesterId {
        @Test
        void shouldGet() {
            when(userService.getUserById(user2.getId())).thenReturn(user2);
            when(itemRequestRepository.findByRequesterId_IdOrderByCreatedAsc(user2.getId()))
                    .thenReturn(List.of(itemRequest1));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestExtendedDto(any(), any())).thenCallRealMethod();
            List<ItemRequestExtendedDto> results = itemRequestService.getByRequesterId(user2.getId());
            assertEquals(1, results.size());
            ItemRequestExtendedDto result = results.get(0);
            checkItemRequestExtendedDto(itemRequest1, result);
            verify(userService, times(1)).getUserById(user2.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequesterId_IdOrderByCreatedAsc(user2.getId());
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestExtendedDto(any(), any());
        }

        @Test
        void shouldGetEmptyIfNotItemRequests() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemRequestRepository.findByRequesterId_IdOrderByCreatedAsc(user1.getId()))
                    .thenReturn(List.of());
            List<ItemRequestExtendedDto> results = itemRequestService.getByRequesterId(user1.getId());
            assertTrue(results.isEmpty());
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequesterId_IdOrderByCreatedAsc(user1.getId());
        }
    }

    @Nested
    class GetAll {
        @Test
        void shouldGetNotSelfRequests() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemRequestRepository.findByRequesterId_IdNot(user1.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of(itemRequest1)));
            when(itemMapper.toItemDto(any())).thenCallRealMethod();
            when(itemRequestMapper.toItemRequestExtendedDto(any(), any())).thenCallRealMethod();
            List<ItemRequestExtendedDto> results = itemRequestService.getAll(user1.getId(), pageable);
            assertEquals(1, results.size());
            ItemRequestExtendedDto result = results.get(0);
            checkItemRequestExtendedDto(itemRequest1, result);
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequesterId_IdNot(user1.getId(), pageable);
            verify(itemMapper, times(1)).toItemDto(any());
            verify(itemRequestMapper, times(1)).toItemRequestExtendedDto(any(), any());
        }

        @Test
        void shouldGetEmptyIfNotRequests() {
            when(userService.getUserById(user1.getId())).thenReturn(user1);
            when(itemRequestRepository.findByRequesterId_IdNot(user1.getId(), pageable))
                    .thenReturn(new PageImpl<>(List.of()));
            List<ItemRequestExtendedDto> results = itemRequestService.getAll(user1.getId(), pageable);
            assertTrue(results.isEmpty());
            verify(userService, times(1)).getUserById(user1.getId());
            verify(itemRequestRepository, times(1))
                    .findByRequesterId_IdNot(user1.getId(), pageable);
        }
    }
}