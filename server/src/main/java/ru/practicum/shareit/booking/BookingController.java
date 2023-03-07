package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

import static ru.practicum.shareit.user.UserController.*;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    public final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader(headerUserId) Long userId,
                                            @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{id}")
    public BookingResponseDto updateBooking(@RequestHeader(headerUserId) Long userId,
                                            @PathVariable Long id,
                                            @RequestParam Boolean approved) {
        return bookingService.updateBooking(userId, id, approved);
    }

    @GetMapping("/{id}")
    public BookingResponseDto getByIdBooking(@RequestHeader(headerUserId) Long userId,
                                             @PathVariable Long id) {
        return bookingService.getByIdBooking(userId, id);
    }

    @GetMapping
    public List<BookingResponseDto> getAllByBookerId(@RequestHeader(headerUserId) Long userId,
                                                     @RequestParam String state,
                                                     @RequestParam Integer from,
                                                     @RequestParam Integer size) {
        return bookingService.getAllByBookerId(userId, State.valueOf(state), PageRequest.of(from / size, size));
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllByOwnerId(@RequestHeader(headerUserId) Long userId,
                                                    @RequestParam String state,
                                                    @RequestParam Integer from,
                                                    @RequestParam Integer size) {
        return bookingService.getAllByOwnerId(userId, State.valueOf(state), PageRequest.of(from / size, size));
    }
}