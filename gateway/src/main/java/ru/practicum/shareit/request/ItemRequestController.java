package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingController;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(BookingController.headerUserId) Long userId,
                                                @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestClient.createRequest(userId, itemRequestCreateDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getByIdRequest(@RequestHeader(BookingController.headerUserId) Long userId,
                                                 @PathVariable Long id) {
        return itemRequestClient.getByIdRequest(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getByRequesterId(@RequestHeader(BookingController.headerUserId) Long userId) {
        return itemRequestClient.getByRequesterId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequest(
            @RequestHeader(BookingController.headerUserId) Long userId,
            @RequestParam(defaultValue = BookingController.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = BookingController.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemRequestClient.getAllRequest(userId, from, size);
    }
}