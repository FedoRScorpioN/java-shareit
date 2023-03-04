package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRepositoryTest {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
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
            .description("SeArCh1 description ")
            .available(true)
            .owner(user1)
            .build();
    private final Item item2 = Item.builder()
            .id(2L)
            .name("Item 2")
            .description("SeARch1 description")
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

    @BeforeEach
    void beforeEach() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
    }

    private void checkItem(Item item1, Item item2) {
        assertEquals(item1.getId(), item2.getId());
        assertEquals(item1.getName(), item2.getName());
        assertEquals(item1.getDescription(), item2.getDescription());
        assertEquals(item1.getAvailable(), item2.getAvailable());
        assertEquals(item1.getOwner().getId(), item2.getOwner().getId());
        assertEquals(item1.getOwner().getName(), item2.getOwner().getName());
        assertEquals(item1.getOwner().getEmail(), item2.getOwner().getEmail());
    }

    @Nested
    class FindByOwnerIdOrderByIdAsc {
        @Test
        void shouldGetTwoItems() {
            List<Item> itemsFromRepository = itemRepository.findByOwnerIdOrderByIdAsc(user1.getId(), pageable)
                    .get()
                    .collect(Collectors.toList());
            assertEquals(2, itemsFromRepository.size());
            Item itemsFromRepository1 = itemsFromRepository.get(0);
            Item itemsFromRepository2 = itemsFromRepository.get(1);
            checkItem(item1, itemsFromRepository1);
            checkItem(item3, itemsFromRepository2);
        }

        @Test
        void shouldGetOneItems() {
            List<Item> itemsFromRepository = itemRepository.findByOwnerIdOrderByIdAsc(user2.getId(), pageable)
                    .get()
                    .collect(Collectors.toList());
            assertEquals(1, itemsFromRepository.size());
            Item itemsFromRepository1 = itemsFromRepository.get(0);
            checkItem(item2, itemsFromRepository1);
        }

        @Test
        void shouldGetZeroItems() {
            List<Item> itemsFromRepository = itemRepository.findByOwnerIdOrderByIdAsc(99L, pageable)
                    .get()
                    .collect(Collectors.toList());
            assertTrue(itemsFromRepository.isEmpty());
        }
    }

    @Nested
    class SearchItem {
        @Test
        void shouldGetTwoAvailableItems() {
            List<Item> itemsFromRepository = itemRepository.search("search1", pageable)
                    .get()
                    .collect(Collectors.toList());
            assertEquals(2, itemsFromRepository.size());
            Item itemsFromRepository1 = itemsFromRepository.get(0);
            Item itemsFromRepository2 = itemsFromRepository.get(1);
            checkItem(item1, itemsFromRepository1);
            checkItem(item2, itemsFromRepository2);
        }

        @Test
        void shouldGetZeroItemsIfItemsNotAvailable() {
            List<Item> itemsFromRepository = itemRepository.search("item3", pageable)
                    .get()
                    .collect(Collectors.toList());
            assertTrue(itemsFromRepository.isEmpty());
        }

        @Test
        void shouldGetZeroItemsIfTextNotFound() {
            List<Item> itemsFromRepository = itemRepository.search("99", pageable)
                    .get()
                    .collect(Collectors.toList());
            assertTrue(itemsFromRepository.isEmpty());
        }
    }
}