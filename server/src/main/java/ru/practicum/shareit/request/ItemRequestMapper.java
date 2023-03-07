package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "requesterId", expression = "java(user)")
    @Mapping(target = "created", expression = "java(dateTime)")
    ItemRequest toItemRequest(ItemRequestCreateDto itemRequestCreateDto, User user, LocalDateTime dateTime);

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "items", expression = "java(items)")
    ItemRequestExtendedDto toItemRequestExtendedDto(ItemRequest itemRequest, List<ItemDto> items);
}