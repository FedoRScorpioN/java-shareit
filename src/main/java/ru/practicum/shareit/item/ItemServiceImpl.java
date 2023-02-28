package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Пользователь с ID {} создал вещь {}.", userId, itemDto);
        userService.getByIdUser(userId);
        itemDto.setOwnerId(userId);
        return itemMapper.toItemDto(itemRepository.save(itemMapper.toItem(itemDto)));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long id, ItemDto itemDto) {
        log.info("Пользователь с ID {} обновил вещь {} с ID.", userId, itemDto, id);
        Item repoItem = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещи с таким ID не существует."));
        if (!Objects.equals(userId, repoItem.getOwner().getId())) {
            throw new ForbiddenException("Изменять вещь может только владелец.");
        }
        itemDto.setOwnerId(userId);
        itemDto.setId(id);
        Item item = itemMapper.toItem(itemDto);
        if (item.getName() != null) {
            repoItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            repoItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            repoItem.setAvailable(item.getAvailable());
        }
        return itemMapper.toItemDto(itemRepository.save(repoItem));
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        log.info("Удалена вещь с ID {}.", id);
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemExtendedDto> getByOwnerId(Long userId) {
        log.info("Выведены все вещи пользователя с ID {}.", userId);
        return itemRepository.getAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(itemMapper::toItemExtendedDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemExtendedDto getByIdItem(Long userId, Long id) {
        log.info("Выведена вещь с ID {}.", id);
        ItemExtendedDto itemExtendedDto = itemMapper.toItemExtendedDto(itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь с указанным ID отсутствует в каталоге.")));
        if (!Objects.equals(userId, itemExtendedDto.getOwnerId())) {
            itemExtendedDto.setLastBooking(null);
            itemExtendedDto.setNextBooking(null);
        }
        return itemExtendedDto;
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        log.info("Поиск вещей с подстрокой \"{}\".", text);
        if (text.isBlank() || text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addCommentItem(Long userId, Long id, CommentRequestDto commentRequestDto) {
        log.info("Добавление комментария пользователем с ID {} к вещи с ID {}.", userId, id);
        commentRequestDto.setAuthorId(userId);
        commentRequestDto.setItemId(id);
        commentRequestDto.setCreated(LocalDateTime.now());
        Comment comment = itemMapper.commentRequestDtoToComment(commentRequestDto);
        if (bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(
                id, userId, LocalDateTime.now(), Status.APPROVED).isEmpty()) {
            throw new BookingException("Пользователь не брал данную вещь в аренду.");
        }
        return itemMapper.commentToCommentDto(commentRepository.save(comment));
    }
}