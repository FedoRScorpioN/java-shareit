package ru.practicum.shareit.request;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;
    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto itemRequestCreateDto) {
        log.info("Создание запроса вещи {} пользователем с id {}.", itemRequestCreateDto, userId);
        User user = userService.getUserById(userId);
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestCreateDto, user, LocalDateTime.now());
        return itemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }
    @Override
    public ItemRequestExtendedDto getByIdRequest(Long userId, Long id) {
        log.info("Вывод запроса вещи с id {} пользователем с id {}.", id, userId);
        userService.getUserById(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Запроса вещи с таким id не существует."));
        List<ItemDto> items = itemRequest.getItems().stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
        return itemRequestMapper.toItemRequestExtendedDto(itemRequest, items);
    }
    @Override
    public List<ItemRequestExtendedDto> getByRequesterId(Long userId) {
        log.info("Вывод всех запросов вещей пользователем с id {}.", userId);
        userService.getUserById(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findByRequesterId_IdOrderByCreatedAsc(userId);
        return itemRequests.stream()
                .map((itemRequest) -> itemRequestMapper.toItemRequestExtendedDto(
                        itemRequest,
                        itemRequest.getItems()
                                .stream()
                                .map(itemMapper::toItemDto)
                                .collect(Collectors.toList()))
                )
                .collect(Collectors.toList());
    }
    @Override
    public List<ItemRequestExtendedDto> getAllRequest(Long userId, Pageable pageable) {
        log.info("Вывод всех запросов вещей постранично {}.", pageable);
        userService.getUserById(userId);
        return itemRequestRepository.findByRequesterId_IdNot(userId, pageable).stream()
                .map((itemRequest) -> itemRequestMapper.toItemRequestExtendedDto(
                        itemRequest,
                        itemRequest.getItems()
                                .stream()
                                .map(itemMapper::toItemDto)
                                .collect(Collectors.toList()))
                )
                .collect(Collectors.toList());
    }
}