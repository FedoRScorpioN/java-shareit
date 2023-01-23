package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> getItemsByOwner(Long userId);

    Item getItemById(Long id);

    Item createItem(Item item);

    Item updateItem(Item item);

    Boolean deleteItem(Long id);

    List<Item> search(String text);
}