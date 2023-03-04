package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.practicum.shareit.user.UserController.PAGE_DEFAULT_FROM;
import static ru.practicum.shareit.user.UserController.PAGE_DEFAULT_SIZE;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RequestBranchesTest {
    private final UserController userController;
    private final ItemController itemController;
    private final ItemRequestController itemRequestController;

    private void checkItemDto(ItemDto itemDto, ItemDto resultItemDto) {
        assertEquals(itemDto.getId(), resultItemDto.getId());
        assertEquals(itemDto.getDescription(), resultItemDto.getDescription());
        assertEquals(itemDto.getAvailable(), resultItemDto.getAvailable());
        assertEquals(itemDto.getOwnerId(), resultItemDto.getOwnerId());
        assertEquals(itemDto.getRequestId(), resultItemDto.getRequestId());
    }

    @Nested
    class CreateUser {
        @Test
        void shouldCreate() {
            UserDto userDto1 = UserDto.builder()
                    .id(1L)
                    .name("User 1")
                    .email("mail1@yandex.ru")
                    .build();
            userController.createUser(userDto1);
            ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.create(userDto1.getId(), itemRequestCreateDto);
            assertEquals(1L, itemRequestDto.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestDto.getDescription());
            assertNotNull(itemRequestDto.getCreated());
        }
    }

    @Nested
    class GetByIdUser {
        @Test
        void shouldGetWithItems() {
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
            ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.create(userDto1.getId(), itemRequestCreateDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(itemRequestDto.getId())
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            ItemRequestExtendedDto itemRequestFromController = itemRequestController.getById(userDto1.getId(), itemRequestDto.getId());
            assertEquals(1L, itemRequestFromController.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestFromController.getDescription());
            assertNotNull(itemRequestFromController.getCreated());
            assertNotNull(itemRequestFromController.getItems());
            assertEquals(1, itemRequestFromController.getItems().size());
            ItemDto itemFromResult = itemRequestFromController.getItems().get(0);
            checkItemDto(itemDto, itemFromResult);
        }
    }

    @Nested
    class GetByRequesterId {
        @Test
        void shouldGetWithItems() {
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
            ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.create(userDto1.getId(), itemRequestCreateDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(itemRequestDto.getId())
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            List<ItemRequestExtendedDto> itemRequestsFromController = itemRequestController.getByRequesterId(userDto1.getId());
            assertEquals(1, itemRequestsFromController.size());
            ItemRequestExtendedDto itemRequestFromController = itemRequestsFromController.get(0);
            assertEquals(1L, itemRequestFromController.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestFromController.getDescription());
            assertNotNull(itemRequestFromController.getCreated());
            assertNotNull(itemRequestFromController.getItems());
            assertEquals(1, itemRequestFromController.getItems().size());
            ItemDto itemFromResult = itemRequestFromController.getItems().get(0);
            checkItemDto(itemDto, itemFromResult);
        }
    }

    @Nested
    class GetAll {
        @Test
        void shouldGetAllWhereNotOwner() {
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
            ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                    .description("description")
                    .build();
            ItemRequestDto itemRequestDto = itemRequestController.create(userDto1.getId(), itemRequestCreateDto);
            ItemDto itemDto = ItemDto.builder()
                    .id(1L)
                    .name("Item")
                    .description("Item description")
                    .available(true)
                    .ownerId(userDto2.getId())
                    .requestId(itemRequestDto.getId())
                    .build();
            itemController.createItem(itemDto.getOwnerId(), itemDto);
            List<ItemRequestExtendedDto> itemRequestsFromController = itemRequestController.getAll(
                    userDto2.getId(),
                    Integer.parseInt(PAGE_DEFAULT_FROM),
                    Integer.parseInt(PAGE_DEFAULT_SIZE));
            assertEquals(1, itemRequestsFromController.size());
            ItemRequestExtendedDto itemRequestFromController = itemRequestsFromController.get(0);
            assertEquals(1L, itemRequestFromController.getId());
            assertEquals(itemRequestCreateDto.getDescription(), itemRequestFromController.getDescription());
            assertNotNull(itemRequestFromController.getCreated());
            assertNotNull(itemRequestFromController.getItems());
            assertEquals(1, itemRequestFromController.getItems().size());
            ItemDto itemFromResult = itemRequestFromController.getItems().get(0);
            checkItemDto(itemDto, itemFromResult);
        }
    }
}