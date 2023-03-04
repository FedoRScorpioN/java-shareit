package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto);

    BookingResponseDto updateBooking(Long userId, Long id, Boolean approved);

    BookingResponseDto getByIdBooking(Long userId, Long id);

    List<BookingResponseDto> getAllByBookerId(Long userId, State state, Pageable pageable);

    List<BookingResponseDto> getAllByOwnerId(Long userId, State state, Pageable pageable);
}