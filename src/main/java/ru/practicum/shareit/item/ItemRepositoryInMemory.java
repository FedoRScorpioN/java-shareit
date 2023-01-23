package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryInMemory implements ItemRepository {
    private final UserService userService;
    private final Map<Long, Item> items = new HashMap<>();
    private Long idMax = 1L;

    @Override
    public Item createItem(Item item) {
        userService.getUserById(item.getOwnerId());
        item.setId(idMax++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        userService.getUserById(item.getOwnerId());
        Item repoItem = getItemById(item.getId());
        if (!Objects.equals(item.getOwnerId(), repoItem.getOwnerId())) {
            throw new ForbiddenException("Изменять вещь может только владелец.");
        }
        if (item.getName() != null) {
            repoItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            repoItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            repoItem.setAvailable(item.getAvailable());
        }
        return items.put(repoItem.getId(), repoItem);
    }

    @Override
    public boolean deleteItem(Long id) {
        items.remove(id);
        return true;
    }

    @Override
    public List<Item> search(String text) {
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(text) || item.getDescription().toLowerCase().contains(text))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> getItemsByOwner(Long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemById(Long id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            throw new NotFoundException("Вещь с указанным ID отсутствует в каталоге.");
        }
    }
}