package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.user.UserController.headerUserId;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(headerUserId) Long userId,
                                 @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto) {
        return itemRequestService.create(userId, itemRequestCreateDto);
    }

    @GetMapping("/{id}")
    public ItemRequestExtendedDto getById(@RequestHeader(headerUserId) Long userId,
                                          @PathVariable Long id) {
        return itemRequestService.getById(userId, id);
    }

    @GetMapping
    public List<ItemRequestExtendedDto> getByRequesterId(@RequestHeader(headerUserId) Long userId) {
        return itemRequestService.getByRequesterId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestExtendedDto> getAll(
            @RequestHeader(headerUserId) Long userId,
            @RequestParam(defaultValue = UserController.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = UserController.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return itemRequestService.getAll(userId, PageRequest.of(from / size, size));
    }
}