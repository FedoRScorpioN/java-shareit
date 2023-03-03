package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface MapperService {
    User getUserById(Long id);

    Item getItemById(Long id);

    List<Booking> getItemLastBooking(Long id, LocalDateTime date, Status status);

    List<Booking> getItemNextBooking(Long id, LocalDateTime date, Status status);

    List<Comment> getItemComments(Long id);
}