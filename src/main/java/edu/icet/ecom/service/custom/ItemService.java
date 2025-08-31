package edu.icet.ecom.service.custom;

import edu.icet.ecom.model.dto.ItemDto;
import java.util.List;

public interface ItemService {
    List<ItemDto> getAllItems();
    ItemDto getItemById(String id);
    List<ItemDto> getOppositeItems(String itemType);
}