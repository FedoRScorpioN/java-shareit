package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ItemMapper.class})
public interface BookingMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "item", expression = "java(item)")
    @Mapping(target = "booker", expression = "java(user)")
    Booking requestDtoToBooking(BookingRequestDto bookingRequestDto, Item item, User user, Status status);

    BookingResponseDto bookingToBookingResponseDto(Booking booking);
}