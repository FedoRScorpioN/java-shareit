package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.validator.Create;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.user.UserController.headerUserId;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    public final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(headerUserId) Long userId,
                              @Validated(Create.class) @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{id}")  //Проверить
    public ItemDto updateItem(@RequestHeader(headerUserId) Long userId,
                              @PathVariable Long id,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, id, itemDto);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
    }

    @GetMapping
    public List<ItemExtendedDto> getByOwnerId(
            @RequestHeader(headerUserId) Long userId,
            @RequestParam(defaultValue = UserController.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = UserController.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemService.getByOwnerId(userId, PageRequest.of(from / size, size));
    }

    @GetMapping("/{id}")
    public ItemExtendedDto getByIdItem(@RequestHeader(headerUserId) Long userId,
                                       @PathVariable Long id) {
        return itemService.getByIdItem(userId, id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(
            @RequestParam String text,
            @RequestParam(defaultValue = UserController.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = UserController.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemService.searchItem(text, PageRequest.of(from / size, size));
    }

    @PostMapping("{id}/comment")
    public CommentDto addCommentItem(@RequestHeader(headerUserId) long userId,
                                     @PathVariable long id,
                                     @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return itemService.addCommentItem(userId, id, commentRequestDto);
    }
}