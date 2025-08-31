//// ItemServiceClient.java
//package edu.icet.ecom.client;
//
//import edu.icet.ecom.model.dto.ItemDto;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@FeignClient(name = "item-service", url = "http://localhost:8082")
//public interface ItemServiceClient {
//
//    @GetMapping("/api/items/{itemId}")
//    ItemDto getItemById(@PathVariable String itemId);
//
//    @GetMapping("/api/items/opposite/{itemType}")
//    List<ItemDto> getOppositeItems(@PathVariable String itemType);
//
//    @DeleteMapping("/api/items/{itemId}")
//    ResponseEntity<String> deleteItem(@PathVariable String itemId);
//
//    @GetMapping("/api/items/health")
//    ResponseEntity<String> healthCheck();
//}

// Updated ItemServiceClient.java with correct endpoints
package edu.icet.ecom.client;

import edu.icet.ecom.model.dto.ItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "item-service", url = "http://localhost:8082")
public interface ItemServiceClient {

    // Get specific item by ID - should match your item service endpoints
    @GetMapping("/api/lost-items/{itemId}")
    ItemDto getLostItemById(@PathVariable String itemId);

    @GetMapping("/api/found-items/{itemId}")
    ItemDto getFoundItemById(@PathVariable String itemId);

    // Generic method that tries both endpoints
    default ItemDto getItemById(String itemId) {
        try {
            // First try as lost item
            return getLostItemById(itemId);
        } catch (Exception e) {
            // If not found, try as found item
            return getFoundItemById(itemId);
        }
    }

    // Get all found items (opposite of lost)
    @GetMapping("/api/found-items")
    List<ItemDto> getFoundItems();

    // Get all lost items (opposite of found)
    @GetMapping("/api/lost-items")
    List<ItemDto> getLostItems();

    // Updated method to get opposite items
    default List<ItemDto> getOppositeItems(String itemType) {
        if ("LOST".equalsIgnoreCase(itemType)) {
            return getFoundItems();
        } else {
            return getLostItems();
        }
    }

    // Delete item - needs to try both endpoints
    @DeleteMapping("/api/lost-items/{itemId}")
    ResponseEntity<String> deleteLostItem(@PathVariable String itemId);

    @DeleteMapping("/api/found-items/{itemId}")
    ResponseEntity<String> deleteFoundItem(@PathVariable String itemId);

    // Generic delete that tries both
    default ResponseEntity<String> deleteItem(String itemId) {
        try {
            // Try deleting as lost item first
            return deleteLostItem(itemId);
        } catch (Exception e) {
            // If not found, try as found item
            return deleteFoundItem(itemId);
        }
    }

    @GetMapping("/api/health")
    ResponseEntity<String> healthCheck();
}