package ru.practicum.shareit.item;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long id, ItemDto itemDto);

    void deleteItem(Long id);

    List<ItemExtendedDto> getByOwnerId(Long userId, Pageable pageable);

    ItemExtendedDto getByIdItem(Long userId, Long id);

    List<ItemDto> searchItem(String text, Pageable pageable);

    CommentDto addCommentItem(Long userId, Long id, CommentRequestDto commentRequestDto);

    Item getItemById(Long id);
}