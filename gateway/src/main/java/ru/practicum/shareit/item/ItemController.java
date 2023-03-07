package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.validator.Create;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.user.UserController.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(headerUserId) Long userId,
                                             @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateItem(@RequestHeader(headerUserId) Long userId,
                                             @PathVariable Long id,
                                             @RequestBody ItemDto itemDto) {
        return itemClient.updateItem(userId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itemClient.deleteItem(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(
            @RequestParam String text,
            @RequestParam(defaultValue = PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemClient.searchItem(text, from, size);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwnerId(
            @RequestHeader(headerUserId) Long userId,
            @RequestParam(defaultValue = PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemClient.getByOwnerId(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@RequestHeader(headerUserId) Long userId,
                                          @PathVariable Long id) {
        return itemClient.getByIdItem(userId, id);
    }

    @PostMapping("{id}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(headerUserId) Long userId,
                                             @PathVariable Long id,
                                             @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemClient.addCommentItem(userId, id, commentRequestDto);
    }
}