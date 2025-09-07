////package edu.icet.ecom.controller;
////
////import edu.icet.ecom.model.dto.ItemDto;
////import edu.icet.ecom.service.custom.ItemService;
////import lombok.RequiredArgsConstructor;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@RestController
////@RequestMapping("/api/items")
////@RequiredArgsConstructor
////@CrossOrigin
////public class ItemController {
////
////    private final ItemService itemService;
////
////    @GetMapping("/all")
////    public ResponseEntity<List<ItemDto>> getAllItems() {
////        List<ItemDto> allItems = itemService.getAllItems();
////        return ResponseEntity.ok(allItems);
////    }
////
////    @GetMapping("/{id}")
////    public ResponseEntity<ItemDto> getItemById(@PathVariable String id) {
////        ItemDto item = itemService.getItemById(id);
////        return ResponseEntity.ok(item);
////    }
////
////    @GetMapping("/opposite/{itemType}")
////    public ResponseEntity<List<ItemDto>> getOppositeItems(@PathVariable String itemType) {
////        List<ItemDto> oppositeItems = itemService.getOppositeItems(itemType);
////        return ResponseEntity.ok(oppositeItems);
////    }
////}
//
//
//// FIXED: ItemController.java - Unified endpoints for claimed service
////package edu.icet.ecom.controller;
////
////import edu.icet.ecom.model.dto.ItemDto;
////import edu.icet.ecom.service.custom.ItemService;
////import edu.icet.ecom.service.custom.LostItemService;
////import edu.icet.ecom.service.custom.FoundItemService;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@RestController
////@RequestMapping("/api/items")
////@RequiredArgsConstructor
////@CrossOrigin
////@Slf4j
////public class ItemController {
////
////    private final ItemService itemService;
////    private final LostItemService lostItemService;
////    private final FoundItemService foundItemService;
////
////    @GetMapping("/all")
////    public ResponseEntity<List<ItemDto>> getAllItems() {
////        List<ItemDto> allItems = itemService.getAllItems();
////        return ResponseEntity.ok(allItems);
////    }
////
////    // UNIFIED: Get item by ID (accepts both "123" and "lost-123" formats)
////    @GetMapping("/{id}")
////    public ResponseEntity<ItemDto> getItemById(@PathVariable String id) {
////        ItemDto item = itemService.getItemById(id);
////        return ResponseEntity.ok(item);
////    }
////
////    // SPECIFIC: Get lost item by ID (for claimed service compatibility)
////    @GetMapping("/lost/{id}")
////    public ResponseEntity<ItemDto> getLostItemById(@PathVariable String id) {
////        try {
////            // Extract numeric ID
////            String numericId = id.replaceAll("^(lost-|found-)", "");
////            Long itemId = Long.parseLong(numericId);
////
////            log.info("Fetching lost item with ID: {} (from input: {})", itemId, id);
////
////            var lostItem = lostItemService.getLostItemById(itemId);
////            if (lostItem == null) {
////                log.warn("Lost item not found with ID: {}", itemId);
////                return ResponseEntity.notFound().build();
////            }
////
////            // Convert to ItemDto using the service
////            ItemDto itemDto = itemService.getItemById(id);
////            if (itemDto == null || !"LOST".equals(itemDto.getItemType())) {
////                log.warn("Item {} is not a lost item or not found", id);
////                return ResponseEntity.notFound().build();
////            }
////
////            log.info("Successfully retrieved lost item: {}", itemDto.getTitle());
////            return ResponseEntity.ok(itemDto);
////
////        } catch (NumberFormatException e) {
////            log.error("Invalid item ID format: {}", id);
////            return ResponseEntity.badRequest().build();
////        } catch (Exception e) {
////            log.error("Error fetching lost item {}: {}", id, e.getMessage());
////            return ResponseEntity.internalServerError().build();
////        }
////    }
////
////    // SPECIFIC: Get found item by ID (for claimed service compatibility)
////    @GetMapping("/found/{id}")
////    public ResponseEntity<ItemDto> getFoundItemById(@PathVariable String id) {
////        try {
////            // Extract numeric ID
////            String numericId = id.replaceAll("^(lost-|found-)", "");
////            Long itemId = Long.parseLong(numericId);
////
////            log.info("Fetching found item with ID: {} (from input: {})", itemId, id);
////
////            var foundItem = foundItemService.getFoundItemById(itemId);
////            if (foundItem == null) {
////                log.warn("Found item not found with ID: {}", itemId);
////                return ResponseEntity.notFound().build();
////            }
////
////            // Convert to ItemDto using the service
////            ItemDto itemDto = itemService.getItemById(id);
////            if (itemDto == null || !"FOUND".equals(itemDto.getItemType())) {
////                log.warn("Item {} is not a found item or not found", id);
////                return ResponseEntity.notFound().build();
////            }
////
////            log.info("Successfully retrieved found item: {}", itemDto.getTitle());
////            return ResponseEntity.ok(itemDto);
////
////        } catch (NumberFormatException e) {
////            log.error("Invalid item ID format: {}", id);
////            return ResponseEntity.badRequest().build();
////        } catch (Exception e) {
////            log.error("Error fetching found item {}: {}", id, e.getMessage());
////            return ResponseEntity.internalServerError().build();
////        }
////    }
////
////    @GetMapping("/opposite/{itemType}")
////    public ResponseEntity<List<ItemDto>> getOppositeItems(@PathVariable String itemType) {
////        List<ItemDto> oppositeItems = itemService.getOppositeItems(itemType);
////        return ResponseEntity.ok(oppositeItems);
////    }
////
////    // DELETE endpoints for claimed service
////    @DeleteMapping("/lost/{id}")
////    public ResponseEntity<String> deleteLostItem(@PathVariable String id) {
////        try {
////            String numericId = id.replaceAll("^(lost-|found-)", "");
////            Long itemId = Long.parseLong(numericId);
////
////            log.info("Deleting lost item with ID: {} (from input: {})", itemId, id);
////            lostItemService.deleteLostItem(itemId);
////
////            return ResponseEntity.ok("Lost item deleted successfully");
////        } catch (NumberFormatException e) {
////            log.error("Invalid item ID format for deletion: {}", id);
////            return ResponseEntity.badRequest().body("Invalid item ID format");
////        } catch (Exception e) {
////            log.error("Error deleting lost item {}: {}", id, e.getMessage());
////            return ResponseEntity.internalServerError().body("Failed to delete item");
////        }
////    }
////
////    @DeleteMapping("/found/{id}")
////    public ResponseEntity<String> deleteFoundItem(@PathVariable String id) {
////        try {
////            String numericId = id.replaceAll("^(lost-|found-)", "");
////            Long itemId = Long.parseLong(numericId);
////
////            log.info("Deleting found item with ID: {} (from input: {})", itemId, id);
////            foundItemService.deleteFoundItem(itemId);
////
////            return ResponseEntity.ok("Found item deleted successfully");
////        } catch (NumberFormatException e) {
////            log.error("Invalid item ID format for deletion: {}", id);
////            return ResponseEntity.badRequest().body("Invalid item ID format");
////        } catch (Exception e) {
////            log.error("Error deleting found item {}: {}", id, e.getMessage());
////            return ResponseEntity.internalServerError().body("Failed to delete item");
////        }
////    }
////}
//
//package edu.icet.ecom.controller;
//
//import edu.icet.ecom.model.dto.ItemDto;
//import edu.icet.ecom.service.custom.ItemService;
//import edu.icet.ecom.service.custom.LostItemService;
//import edu.icet.ecom.service.custom.FoundItemService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/items")
//@RequiredArgsConstructor
//@CrossOrigin
//@Slf4j
//public class ItemController {
//
//    private final ItemService itemService;
//    private final LostItemService lostItemService;
//    private final FoundItemService foundItemService;
//
//    @GetMapping("/all")
//    public ResponseEntity<List<ItemDto>> getAllItems() {
//        List<ItemDto> allItems = itemService.getAllItems();
//        return ResponseEntity.ok(allItems);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ItemDto> getItemById(@PathVariable String id) {
//        ItemDto item = itemService.getItemById(id);
//        return ResponseEntity.ok(item);
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<List<ItemDto>> getAllItemsUnified() {
//        try {
//            log.info("Fetching ALL items for external services");
//            List<ItemDto> allItems = itemService.getAllItems();
//            log.info("Returning {} total items", allItems.size());
//            return ResponseEntity.ok(allItems);
//        } catch (Exception e) {
//            log.error("Error fetching all items: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//
//    @GetMapping("/lost/{id}")
//    public ResponseEntity<ItemDto> getLostItemById(@PathVariable String id) {
//        try {
//            String numericId = id.replaceAll("^(lost-|found-)", "");
//            Long itemId = Long.parseLong(numericId);
//
//            log.info("Fetching lost item with ID: {} (from input: {})", itemId, id);
//
//            var lostItem = lostItemService.getLostItemById(itemId);
//            if (lostItem == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ItemDto itemDto = itemService.getItemById(numericId);
//            if (itemDto == null || !"LOST".equals(itemDto.getItemType())) {
//                return ResponseEntity.notFound().build();
//            }
//
//            return ResponseEntity.ok(itemDto);
//
//        } catch (NumberFormatException e) {
//            return ResponseEntity.badRequest().build();
//        } catch (Exception e) {
//            log.error("Error fetching lost item {}: {}", id, e.getMessage());
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    @GetMapping("/found/{id}")
//    public ResponseEntity<ItemDto> getFoundItemById(@PathVariable String id) {
//        try {
//            String numericId = id.replaceAll("^(lost-|found-)", "");
//            Long itemId = Long.parseLong(numericId);
//
//            var foundItem = foundItemService.getFoundItemById(itemId);
//            if (foundItem == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            ItemDto itemDto = itemService.getItemById(numericId);
//            if (itemDto == null || !"FOUND".equals(itemDto.getItemType())) {
//                return ResponseEntity.notFound().build();
//            }
//
//            return ResponseEntity.ok(itemDto);
//
//        } catch (NumberFormatException e) {
//            return ResponseEntity.badRequest().build();
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    @GetMapping("/opposite/{itemType}")
//    public ResponseEntity<List<ItemDto>> getOppositeItems(@PathVariable String itemType) {
//        List<ItemDto> oppositeItems = itemService.getOppositeItems(itemType);
//        return ResponseEntity.ok(oppositeItems);
//    }
//
//    // CRITICAL FIX: Add proper deletion endpoints for claimed service
//    @DeleteMapping("/lost/{id}")
//    public ResponseEntity<String> deleteLostItem(@PathVariable String id) {
//        try {
//            String numericId = id.replaceAll("^(lost-|found-)", "");
//            Long itemId = Long.parseLong(numericId);
//
//            log.info("Deleting lost item with ID: {} (from input: {})", itemId, id);
//            lostItemService.deleteLostItem(itemId);
//
//            return ResponseEntity.ok("Lost item deleted successfully");
//        } catch (NumberFormatException e) {
//            log.error("Invalid item ID format for deletion: {}", id);
//            return ResponseEntity.badRequest().body("Invalid item ID format");
//        } catch (Exception e) {
//            log.error("Error deleting lost item {}: {}", id, e.getMessage());
//            return ResponseEntity.internalServerError().body("Failed to delete item");
//        }
//    }
//
//    @DeleteMapping("/found/{id}")
//    public ResponseEntity<String> deleteFoundItem(@PathVariable String id) {
//        try {
//            String numericId = id.replaceAll("^(lost-|found-)", "");
//            Long itemId = Long.parseLong(numericId);
//
//            log.info("Deleting found item with ID: {} (from input: {})", itemId, id);
//            foundItemService.deleteFoundItem(itemId);
//
//            return ResponseEntity.ok("Found item deleted successfully");
//        } catch (NumberFormatException e) {
//            log.error("Invalid item ID format for deletion: {}", id);
//            return ResponseEntity.badRequest().body("Invalid item ID format");
//        } catch (Exception e) {
//            log.error("Error deleting found item {}: {}", id, e.getMessage());
//            return ResponseEntity.internalServerError().body("Failed to delete item");
//        }
//    }
//}

package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.ItemDto;
import edu.icet.ecom.service.custom.ItemService;
import edu.icet.ecom.service.custom.LostItemService;
import edu.icet.ecom.service.custom.FoundItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;

    // FIXED: Single endpoint for getting all items
    @GetMapping("/all")
    public ResponseEntity<List<ItemDto>> getAllItems() {
        try {
            log.info("Fetching ALL items for external services");
            List<ItemDto> allItems = itemService.getAllItems();
            log.info("Returning {} total items", allItems.size());
            return ResponseEntity.ok(allItems);
        } catch (Exception e) {
            log.error("Error fetching all items: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> getItemById(@PathVariable String id) {
        try {
            log.info("Fetching item by ID: {}", id);
            ItemDto item = itemService.getItemById(id);
            if (item == null) {
                log.warn("Item not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            log.error("Error fetching item {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/lost/{id}")
    public ResponseEntity<ItemDto> getLostItemById(@PathVariable String id) {
        try {
            String numericId = id.replaceAll("^(lost-|found-)", "");
            Long itemId = Long.parseLong(numericId);

            log.info("Fetching lost item with ID: {} (from input: {})", itemId, id);

            var lostItem = lostItemService.getLostItemById(itemId);
            if (lostItem == null) {
                log.warn("Lost item not found with ID: {}", itemId);
                return ResponseEntity.notFound().build();
            }

            // Use the numeric ID to get the ItemDto
            ItemDto itemDto = itemService.getItemById(numericId);
            if (itemDto == null) {
                log.warn("ItemDto not found for lost item with ID: {}", numericId);
                return ResponseEntity.notFound().build();
            }

            // Ensure itemType is set correctly
            if (!"LOST".equals(itemDto.getItemType())) {
                log.warn("Item {} is not a lost item, found type: {}", numericId, itemDto.getItemType());
                itemDto.setItemType("LOST"); // Force correct type
            }

            log.info("Successfully retrieved lost item: {}", itemDto.getTitle());
            return ResponseEntity.ok(itemDto);

        } catch (NumberFormatException e) {
            log.error("Invalid item ID format: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching lost item {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/found/{id}")
    public ResponseEntity<ItemDto> getFoundItemById(@PathVariable String id) {
        try {
            String numericId = id.replaceAll("^(lost-|found-)", "");
            Long itemId = Long.parseLong(numericId);

            log.info("Fetching found item with ID: {} (from input: {})", itemId, id);

            var foundItem = foundItemService.getFoundItemById(itemId);
            if (foundItem == null) {
                log.warn("Found item not found with ID: {}", itemId);
                return ResponseEntity.notFound().build();
            }

            // Use the numeric ID to get the ItemDto
            ItemDto itemDto = itemService.getItemById(numericId);
            if (itemDto == null) {
                log.warn("ItemDto not found for found item with ID: {}", numericId);
                return ResponseEntity.notFound().build();
            }

            // Ensure itemType is set correctly
            if (!"FOUND".equals(itemDto.getItemType())) {
                log.warn("Item {} is not a found item, found type: {}", numericId, itemDto.getItemType());
                itemDto.setItemType("FOUND"); // Force correct type
            }

            log.info("Successfully retrieved found item: {}", itemDto.getTitle());
            return ResponseEntity.ok(itemDto);

        } catch (NumberFormatException e) {
            log.error("Invalid item ID format: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching found item {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/opposite/{itemType}")
    public ResponseEntity<List<ItemDto>> getOppositeItems(@PathVariable String itemType) {
        try {
            log.info("Fetching opposite items for type: {}", itemType);
            List<ItemDto> oppositeItems = itemService.getOppositeItems(itemType);
            log.info("Found {} opposite items for type: {}", oppositeItems.size(), itemType);
            return ResponseEntity.ok(oppositeItems);
        } catch (Exception e) {
            log.error("Error fetching opposite items for type {}: {}", itemType, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/lost/{id}")
    public ResponseEntity<String> deleteLostItem(@PathVariable String id) {
        try {
            String numericId = id.replaceAll("^(lost-|found-)", "");
            Long itemId = Long.parseLong(numericId);

            log.info("Deleting lost item with ID: {} (from input: {})", itemId, id);

            // Check if item exists before deleting
            var existingItem = lostItemService.getLostItemById(itemId);
            if (existingItem == null) {
                log.warn("Cannot delete - lost item not found with ID: {}", itemId);
                return ResponseEntity.notFound().build();
            }

            lostItemService.deleteLostItem(itemId);
            log.info("Successfully deleted lost item with ID: {}", itemId);

            return ResponseEntity.ok("Lost item deleted successfully");

        } catch (NumberFormatException e) {
            log.error("Invalid item ID format for deletion: {}", id);
            return ResponseEntity.badRequest().body("Invalid item ID format");
        } catch (Exception e) {
            log.error("Error deleting lost item {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to delete item: " + e.getMessage());
        }
    }

    @DeleteMapping("/found/{id}")
    public ResponseEntity<String> deleteFoundItem(@PathVariable String id) {
        try {
            String numericId = id.replaceAll("^(lost-|found-)", "");
            Long itemId = Long.parseLong(numericId);

            log.info("Deleting found item with ID: {} (from input: {})", itemId, id);

            // Check if item exists before deleting
            var existingItem = foundItemService.getFoundItemById(itemId);
            if (existingItem == null) {
                log.warn("Cannot delete - found item not found with ID: {}", itemId);
                return ResponseEntity.notFound().build();
            }

            foundItemService.deleteFoundItem(itemId);
            log.info("Successfully deleted found item with ID: {}", itemId);

            return ResponseEntity.ok("Found item deleted successfully");

        } catch (NumberFormatException e) {
            log.error("Invalid item ID format for deletion: {}", id);
            return ResponseEntity.badRequest().body("Invalid item ID format");
        } catch (Exception e) {
            log.error("Error deleting found item {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to delete item: " + e.getMessage());
        }
    }

    // Health check endpoint for other services
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Item Service is running");
    }
}