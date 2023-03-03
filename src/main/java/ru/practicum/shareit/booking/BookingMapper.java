package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ItemMapper.class})
public abstract class BookingMapper {
    @Autowired
    private ItemMapper itemMapper;

    @Mapping(source = "bookerId", target = "booker", qualifiedByName = "mapUserDtoFromId")
    @Mapping(source = "itemId", target = "item", qualifiedByName = "mapItemDtoFromId")
    public abstract Booking requestDtoToBooking(BookingRequestDto bookingRequestDto);

    public abstract Booking responseDtoToBooking(BookingResponseDto bookingResponseDto);

    public abstract BookingResponseDto bookingToBookingResponseDto(Booking booking);

    @Named("mapUserDtoFromId")
    User mapUserDtoFromId(Long userId) {
        return itemMapper.mapUserFromUserId(userId);
    }

    @Named("mapItemDtoFromId")
    Item mapItemDtoFromId(Long itemId) {
        return itemMapper.mapItemFromItemId(itemId);
    }
}