package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.validator.Create;
import ru.practicum.shareit.validator.Update;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    public final ItemService itemService;
    private final String headerUserId = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto createItem(@RequestHeader(headerUserId) Long userId,
                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader(headerUserId) Long userId,
                              @PathVariable Long id,
                              @Validated(Update.class) @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @GetMapping
    public List<ItemExtendedDto> getByOwnerId(@RequestHeader(headerUserId) Long userId) {
        return itemService.getByOwnerId(userId);
    }

    @GetMapping("/{id}")
    public ItemExtendedDto getByIdItem(@RequestHeader(headerUserId) Long userId,
                                       @PathVariable Long id) {
        return itemService.getByIdItem(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam String text) {
        return itemService.searchItem(text);
    }

    @PostMapping("{id}/comment")
    public CommentDto addCommentItem(@RequestHeader(headerUserId) long userId,
                                     @PathVariable long id,
                                     @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.addCommentItem(userId, id, commentRequestDto);
    }
}