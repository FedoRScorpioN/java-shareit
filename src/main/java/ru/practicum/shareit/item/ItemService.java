package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long id, ItemDto itemDto);

    Boolean deleteItem(Long id);

    List<ItemDto> search(String text);

    List<ItemDto> getItemsByOwner(Long userId);

    ItemDto getItemById(Long id);
}
