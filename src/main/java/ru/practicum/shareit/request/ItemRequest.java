package ru.practicum.shareit.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class ItemRequest {
    Long id;
    String description;
    Long requestUserId;
    Date created;
}