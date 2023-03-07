package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BookingException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.user.UserController.*;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(headerUserId) Long userId,
                                                @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new BookingException("Недопустимое время брони.");
        }
        return bookingClient.createBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateBooking(@RequestHeader(headerUserId) Long userId,
                                                @PathVariable Long id,
                                                @RequestParam Boolean approved) {
        return bookingClient.updateBooking(userId, id, approved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getByIdBooking(@RequestHeader(headerUserId) Long userId,
                                                 @PathVariable Long id) {
        return bookingClient.getByIdBooking(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBookerId(
            @RequestHeader(headerUserId) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE) @Positive Integer size) {
        State stateEnum = State.stringToState(state)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getAllByBookerId(userId, stateEnum, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwnerId(
            @RequestHeader(headerUserId) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE) @Positive Integer size) {
        State stateEnum = State.stringToState(state).orElseThrow(
                () -> new IllegalArgumentException("Unknown state: " + state));
        return bookingClient.getAllByOwnerId(userId, stateEnum, from, size);
    }
}