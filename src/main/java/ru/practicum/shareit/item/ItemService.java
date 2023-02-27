package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long id, ItemDto itemDto);

    void deleteItem(Long id);

    List<ItemExtendedDto> getByOwnerId(Long userId);

    ItemExtendedDto getByIdItem(Long userId, Long id);

    List<ItemDto> searchItem(String text);

    CommentDto addCommentItem(Long userId, Long id, CommentRequestDto commentRequestDto);
}