package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Пользователь с ID {} создал вещь {}.", userId, itemDto);
        itemDto.setOwnerId(userId);
        return itemMapper.toItemDto(itemRepository.createItem(itemMapper.toItem(itemDto)));
    }

    @Override
    public ItemDto updateItem(Long userId, Long id, ItemDto itemDto) {
        log.info("Пользователь с ID {} создал вещь {} с ID.", userId, itemDto, id);
        itemDto.setOwnerId(userId);
        itemDto.setId(id);
        return itemMapper.toItemDto(itemRepository.updateItem(itemMapper.toItem(itemDto)));
    }

    @Override
    public Boolean deleteItem(Long id) {
        log.info("Удалена вещь с ID {}.", id);
        return itemRepository.deleteItem(id);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по запросу \"{}\".", text);
        if (text.isBlank() || text.isEmpty()) {
            return new ArrayList<>();
        }
        text = text.toLowerCase();
        return itemRepository.search(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        log.info("Выведены все вещи пользователя с ID {}.", userId);
        return itemRepository.getItemsByOwner(userId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long id) {
        log.info("Выведена вещь с ID {}.", id);
        return itemMapper.toItemDto(itemRepository.getItemById(id));
    }
}