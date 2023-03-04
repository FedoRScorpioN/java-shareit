package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestCreateDto itemRequestCreateDto);

    ItemRequestExtendedDto getById(Long userId, Long id);

    List<ItemRequestExtendedDto> getByRequesterId(Long userId);

    List<ItemRequestExtendedDto> getAll(Long userId, Pageable pageable);
}