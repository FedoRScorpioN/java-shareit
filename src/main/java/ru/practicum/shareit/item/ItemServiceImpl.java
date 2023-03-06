package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.exception.BookingException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.Status.APPROVED;

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
        Item item = itemMapper.toItem(itemDto, userService.getUserById(userId));
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long id, ItemDto itemDto) {
        log.info("Пользователь с ID {} обновил вещь {} с ID {}.", userId, itemDto, id);
        Item repoItem = getItemById(id);
        if (!Objects.equals(userId, repoItem.getOwner().getId())) {
            throw new ForbiddenException("Изменять вещь может только владелец.");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            repoItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            repoItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            repoItem.setAvailable(itemDto.getAvailable());
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
    public List<ItemExtendedDto> getByOwnerId(Long userId, Pageable pageable) {
        log.info("Выведены все вещи пользователя с ID {}.", userId);
        return itemRepository.findByOwnerIdOrderByIdAsc(userId, pageable).stream()
                .map((item) -> itemMapper.toItemExtendedDto(item, getLastBooking(item), getNextBooking(item)))
                .collect(Collectors.toList());
    }

    @Override
    public ItemExtendedDto getByIdItem(Long userId, Long id) {
        log.info("Выведена вещь с ID {}.", id);
        Item item = getItemById(id);
        if (!Objects.equals(userId, item.getOwner().getId())) {
            return itemMapper.toItemExtendedDto(item, null, null);
        } else {
            return itemMapper.toItemExtendedDto(item, getLastBooking(item), getNextBooking(item));
        }
    }

    @Override
    public List<ItemDto> searchItem(String text, Pageable pageable) {
        log.info("Поиск вещей с подстрокой \"{}\".", text);
        if (text.isBlank() || text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text, pageable)
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addCommentItem(Long userId, Long id, CommentRequestDto commentRequestDto) {
        log.info("Добавление комментария пользователем с ID {} к вещи с ID {}.", userId, id);
        Comment comment = itemMapper.commentRequestDtoToComment(commentRequestDto,
                LocalDateTime.now(), userService.getUserById(userId), id);
        if (bookingRepository.findByItemIdAndBookerIdAndEndIsBeforeAndStatusEquals(
                id, userId, LocalDateTime.now(), APPROVED).isEmpty()) {
            throw new BookingException("Пользователь не брал данную вещь в аренду.");
        }
        return itemMapper.commentToCommentDto(commentRepository.save(comment));
    }

    @Override
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещи с таким id не существует."));
    }

    private BookingItemDto getLastBooking(Item item) {
        return bookingRepository.findByItemIdAndStartBeforeAndStatusEqualsOrderByStartDesc(
                        item.getId(), LocalDateTime.now(), APPROVED)
                .stream()
                .findFirst()
                .map(itemMapper::bookingToBookingItemDto)
                .orElse(null);
    }

    private BookingItemDto getNextBooking(Item item) {
        return bookingRepository.findByItemIdAndStartAfterAndStatusEqualsOrderByStartAsc(
                        item.getId(), LocalDateTime.now(), APPROVED)
                .stream()
                .findFirst()
                .map(itemMapper::bookingToBookingItemDto)
                .orElse(null);
    }
}