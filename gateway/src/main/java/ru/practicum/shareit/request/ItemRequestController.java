package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.user.UserController.*;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(headerUserId) Long userId,
                                                @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestClient.createRequest(userId, itemRequestCreateDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getByIdRequest(@RequestHeader(headerUserId) Long userId,
                                                 @PathVariable Long id) {
        return itemRequestClient.getByIdRequest(userId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getByRequesterId(@RequestHeader(headerUserId) Long userId) {
        return itemRequestClient.getByRequesterId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequest(
            @RequestHeader(headerUserId) Long userId,
            @RequestParam(defaultValue = PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemRequestClient.getAllRequest(userId, from, size);
    }
}