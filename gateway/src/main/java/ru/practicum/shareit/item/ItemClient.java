package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
@Slf4j
public final class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(Long userId, ItemDto itemDto) {
        log.info("Пользователь с ID {} создал вещь {}.", userId, itemDto);
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(Long userId, Long id, ItemDto itemDto) {
        log.info("Пользователь с ID {} обновил вещь {} с ID {}.", userId, itemDto, id);
        return patch("/" + id, userId, itemDto);
    }

    public void deleteItem(Long id) {
        log.info("Удалена вещь с ID {}.", id);
        delete("/" + id);
    }

    public ResponseEntity<Object> getByOwnerId(Long userId, Integer from, Integer size) {
        log.info("Выведены все вещи пользователя с ID {}.", userId);

        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getByIdItem(Long userId, Long id) {
        log.info("Выведена вещь с ID {}.", id);
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> searchItem(String text, Integer from, Integer size) {
        log.info("Поиск вещей с подстрокой \"{}\".", text);

        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", null, parameters);
    }

    public ResponseEntity<Object> addCommentItem(Long userId, Long id, CommentRequestDto commentDto) {
        log.info("Добавление комментария пользователем с ID {} к вещи с ID {}.", userId, id);
        return post("/" + id + "/comment", userId, commentDto);
    }
}