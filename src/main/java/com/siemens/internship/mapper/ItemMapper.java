package com.siemens.internship.mapper;

import com.siemens.internship.model.Item;
import com.siemens.internship.response.ItemResponse;
import org.springframework.stereotype.Service;

@Service
public class ItemMapper {
    public ItemResponse toItemResponse(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getDescription(),
            item.getStatus(),
            item.getEmail()
        );
    }

    public Item toItem(ItemResponse itemResponse) {
        return Item
                .builder()
                .id(itemResponse.id())
                .name(itemResponse.name())
                .description(itemResponse.description())
                .status(itemResponse.status())
                .email(itemResponse.email())
                .build();
    }
}
