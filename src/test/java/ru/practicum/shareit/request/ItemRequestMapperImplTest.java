package ru.practicum.shareit.request;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class ItemRequestMapperImplTest {
    @InjectMocks
    private ItemRequestMapperImpl itemRequestMapper;
    private final LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
    private final User user = User.builder()
            .id(1L)
            .name("User 1")
            .email("mail1@yandex.ru")
            .build();
    private final List<Item> items = List.of(Item.builder()
            .id(1L)
            .name("Item")
            .description("Item description")
            .available(true)
            .owner(user)
            .requestId(1L)
            .build());
    private final List<ItemDto> itemsDto = List.of(ItemDto.builder()
            .id(1L)
            .name("Item")
            .description("Item description")
            .available(true)
            .ownerId(user.getId())
            .requestId(1L)
            .build());
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .description("ItemRequest1 description")
            .requesterId(user)
            .created(dateTime)
            .items(items)
            .build();
    private final ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
            .description("Item description")
            .build();

    @Nested
    class ToItemRequest {
        @Test
        void shouldReturnItemRequest() {
            ItemRequest result = itemRequestMapper.toItemRequest(itemRequestCreateDto, user, dateTime);
            assertNull(result.getId());
            assertEquals(itemRequestCreateDto.getDescription(), result.getDescription());
            assertEquals(user.getId(), result.getRequesterId().getId());
            assertEquals(user.getName(), result.getRequesterId().getName());
            assertEquals(user.getEmail(), result.getRequesterId().getEmail());
            assertEquals(dateTime, result.getCreated());
            assertNull(result.getItems());
        }

        @Test
        void shouldReturnNull() {
            ItemRequest result = itemRequestMapper.toItemRequest(null, null, null);
            assertNull(result);
        }
    }

    @Nested
    class ToItemRequestDto {
        @Test
        void shouldReturnItemRequestDto() {
            ItemRequestDto result = itemRequestMapper.toItemRequestDto(itemRequest);
            assertEquals(itemRequest.getId(), result.getId());
            assertEquals(itemRequest.getDescription(), result.getDescription());
            assertEquals(itemRequest.getCreated(), result.getCreated());
        }

        @Test
        void shouldReturnNull() {
            ItemRequestDto result = itemRequestMapper.toItemRequestDto(null);
            assertNull(result);
        }
    }

    @Nested
    class ToItemRequestExtendedDto {
        @Test
        void shouldReturnItemRequestExtendedDto() {
            ItemRequestExtendedDto result = itemRequestMapper.toItemRequestExtendedDto(itemRequest, itemsDto);
            assertEquals(itemRequest.getId(), result.getId());
            assertEquals(itemRequest.getDescription(), result.getDescription());
            assertEquals(itemRequest.getCreated(), result.getCreated());
            assertEquals(itemsDto, result.getItems());
        }

        @Test
        void shouldReturnNull() {
            ItemRequestExtendedDto result = itemRequestMapper.toItemRequestExtendedDto(null, null);
            assertNull(result);
        }
    }
}