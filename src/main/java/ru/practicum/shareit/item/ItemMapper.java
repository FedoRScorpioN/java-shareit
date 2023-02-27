package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ItemMapper {
    @Autowired
    private MapperService mapperService;

    @Mapping(source = "owner", target = "ownerId", qualifiedByName = "mapUserIdFromUser")
    public abstract ItemDto toItemDto(Item item);

    @Mapping(source = "ownerId", target = "owner", qualifiedByName = "mapUserFromUserId")
    public abstract Item toItem(ItemDto itemDto);

    @Mapping(source = "owner", target = "ownerId", qualifiedByName = "mapUserIdFromUser")
    @Mapping(target = "lastBooking", expression = "java(addLastBooking(item))")
    @Mapping(target = "nextBooking", expression = "java(addNextBooking(item))")
    @Mapping(target = "comments", expression = "java(addComment(item))")
    public abstract ItemExtendedDto toItemExtendedDto(Item item);

    @Mapping(source = "booker", target = "bookerId", qualifiedByName = "mapUserIdFromUser")
    public abstract BookingItemDto bookingToBookingItemDto(Booking booking);

    @Mapping(source = "authorId", target = "author", qualifiedByName = "mapUserFromUserId")
    @Mapping(source = "itemId", target = "item", qualifiedByName = "mapItemFromItemId")
    public abstract Comment commentRequestDtoToComment(CommentRequestDto commentRequestDto);

    @Mapping(source = "author", target = "authorName", qualifiedByName = "mapUserNameFromUser")
    public abstract CommentDto commentToCommentDto(Comment comment);

    @Named("mapUserIdFromUser")
    public Long mapUserIdFromUser(User user) {
        return user.getId();
    }

    @Named("mapUserNameFromUser")
    public String mapUserNameFromUser(User user) {
        return user.getName();
    }

    @Named("mapUserFromUserId")
    public User mapUserFromUserId(Long userId) {
        return mapperService.getUserById(userId);
    }

    @Named("mapItemFromItemId")
    public Item mapItemFromItemId(Long itemId) {
        return mapperService.getItemById(itemId);
    }

    public BookingItemDto addLastBooking(Item item) {
        List<Booking> bookings = mapperService.getItemLastBooking(item.getId(), LocalDateTime.now(), Status.APPROVED);
        if (bookings.isEmpty()) {
            return null;
        }
        Booking lastBooking = bookings.get(0);
        return bookingToBookingItemDto(lastBooking);
    }

    public BookingItemDto addNextBooking(Item item) {
        List<Booking> bookings = mapperService.getItemNextBooking(item.getId(), LocalDateTime.now(), Status.APPROVED);

        if (bookings.isEmpty()) {
            return null;
        }
        Booking nextBooking = bookings.get(0);
        return bookingToBookingItemDto(nextBooking);
    }

    public List<CommentDto> addComment(Item item) {
        return mapperService.getItemComments(item.getId()).stream()
                .map(this::commentToCommentDto)
                .collect(Collectors.toList());
    }
}