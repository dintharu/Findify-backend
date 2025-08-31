package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.ItemDto;
import edu.icet.ecom.service.custom.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/all")
    public ResponseEntity<List<ItemDto>> getAllItems() {
        List<ItemDto> allItems = itemService.getAllItems();
        return ResponseEntity.ok(allItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable String id) {
        ItemDto item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/opposite/{itemType}")
    public ResponseEntity<List<ItemDto>> getOppositeItems(@PathVariable String itemType) {
        List<ItemDto> oppositeItems = itemService.getOppositeItems(itemType);
        return ResponseEntity.ok(oppositeItems);
    }
}