package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestCreateDto itemRequestCreateDto);

    ItemRequestExtendedDto getByIdRequest(Long userId, Long id);

    List<ItemRequestExtendedDto> getByRequesterId(Long userId);

    List<ItemRequestExtendedDto> getAllRequest(Long userId, Pageable pageable);
}