//////// Fixed ItemServiceClient.java
//////package edu.icet.ecom.client;
//////
//////import edu.icet.ecom.model.dto.ItemDto;
//////import feign.FeignException;
//////import org.springframework.cloud.openfeign.FeignClient;
//////import org.springframework.http.ResponseEntity;
//////import org.springframework.web.bind.annotation.*;
//////
//////import java.util.List;
//////
//////@FeignClient(name = "item-service", url = "http://localhost:8082")
//////public interface ItemServiceClient {
//////
//////    // Separate endpoints for lost and found items
//////    @GetMapping("/api/lost-items/{itemId}")
//////    ItemDto getLostItemById(@PathVariable String itemId);
//////
//////    @GetMapping("/api/found-items/{itemId}")
//////    ItemDto getFoundItemById(@PathVariable String itemId);
//////
//////    // FIXED: Specific methods that don't fall back incorrectly
//////    default ItemDto getLostItemByIdSafe(String itemId) {
//////        try {
//////            ItemDto item = getLostItemById(itemId);
//////            if (item != null && "LOST".equalsIgnoreCase(item.getItemType())) {
//////                return item;
//////            }
//////            throw new RuntimeException("Item " + itemId + " is not a lost item");
//////        } catch (FeignException.NotFound e) {
//////            throw new RuntimeException("Lost item not found with ID: " + itemId);
//////        } catch (FeignException e) {
//////            throw new RuntimeException("Error fetching lost item: " + e.getMessage());
//////        }
//////    }
//////
//////    default ItemDto getFoundItemByIdSafe(String itemId) {
//////        try {
//////            ItemDto item = getFoundItemById(itemId);
//////            if (item != null && "FOUND".equalsIgnoreCase(item.getItemType())) {
//////                return item;
//////            }
//////            throw new RuntimeException("Item " + itemId + " is not a found item");
//////        } catch (FeignException.NotFound e) {
//////            throw new RuntimeException("Found item not found with ID: " + itemId);
//////        } catch (FeignException e) {
//////            throw new RuntimeException("Error fetching found item: " + e.getMessage());
//////        }
//////    }
//////
//////    // Generic method - ONLY use when you don't know the type
//////    default ItemDto getItemById(String itemId) {
//////        // Try lost first, then found
//////        try {
//////            return getLostItemById(itemId);
//////        } catch (FeignException.NotFound e) {
//////            try {
//////                return getFoundItemById(itemId);
//////            } catch (FeignException.NotFound e2) {
//////                throw new RuntimeException("Item not found with ID: " + itemId);
//////            }
//////        }
//////    }
//////
//////    // Get all items
//////    @GetMapping("/api/found-items")
//////    List<ItemDto> getFoundItems();
//////
//////    @GetMapping("/api/lost-items")
//////    List<ItemDto> getLostItems();
//////
//////    // Get opposite items
//////    default List<ItemDto> getOppositeItems(String itemType) {
//////        if ("LOST".equalsIgnoreCase(itemType)) {
//////            return getFoundItems();
//////        } else {
//////            return getLostItems();
//////        }
//////    }
//////
//////    // Delete endpoints
//////    @DeleteMapping("/api/lost-items/{itemId}")
//////    ResponseEntity<String> deleteLostItem(@PathVariable String itemId);
//////
//////    @DeleteMapping("/api/found-items/{itemId}")
//////    ResponseEntity<String> deleteFoundItem(@PathVariable String itemId);
//////
//////    // Safe delete method
//////    default ResponseEntity<String> deleteItem(String itemId) {
//////        try {
//////            return deleteLostItem(itemId);
//////        } catch (FeignException.NotFound e) {
//////            try {
//////                return deleteFoundItem(itemId);
//////            } catch (FeignException.NotFound e2) {
//////                throw new RuntimeException("Item not found for deletion: " + itemId);
//////            }
//////        }
//////    }
//////
//////    @GetMapping("/api/health")
//////    ResponseEntity<String> healthCheck();
//////}
////
////// FIXED: ItemServiceClient.java - Proper endpoint mapping
////package edu.icet.ecom.client;
////
////import edu.icet.ecom.model.dto.ItemDto;
////import feign.FeignException;
////import org.springframework.cloud.openfeign.FeignClient;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@FeignClient(name = "item-service", url = "http://localhost:8082")
////public interface ItemServiceClient {
////
////    // FIXED: Direct endpoints that match your Item Service controllers
////    @GetMapping("/api/lost-items/{itemId}")
////    ItemDto getLostItemById(@PathVariable String itemId);
////
////    @GetMapping("/api/found-items/{itemId}")
////    ItemDto getFoundItemById(@PathVariable String itemId);
////
////    // Get all items endpoints
////    @GetMapping("/api/lost-items")
////    List<ItemDto> getAllLostItems();
////
////    @GetMapping("/api/found-items")
////    List<ItemDto> getAllFoundItems();
////
////    // FIXED: Delete endpoints with proper paths
////    @DeleteMapping("/api/lost-items/{itemId}")
////    ResponseEntity<String> deleteLostItem(@PathVariable String itemId);
////
////    @DeleteMapping("/api/found-items/{itemId}")
////    ResponseEntity<String> deleteFoundItem(@PathVariable String itemId);
////
////    // FIXED: Generic get item that tries both endpoints
////    default ItemDto getItemById(String itemId) {
////        // Try lost first, then found
////        try {
////            return getLostItemById(itemId);
////        } catch (FeignException.NotFound e) {
////            try {
////                return getFoundItemById(itemId);
////            } catch (FeignException.NotFound e2) {
////                throw new RuntimeException("Item not found with ID: " + itemId);
////            }
////        }
////    }
////
////    // FIXED: Get opposite items method
////    default List<ItemDto> getOppositeItems(String itemType) {
////        if ("LOST".equalsIgnoreCase(itemType)) {
////            return getAllFoundItems();
////        } else {
////            return getAllLostItems();
////        }
////    }
////
////    // Health check
////    @GetMapping("/api/items/health")
////    ResponseEntity<String> healthCheck();
////}
////
////// ==== COMPLETE FIXED ItemServiceClient.java ====
////package edu.icet.ecom.client;
////
////import edu.icet.ecom.model.dto.ItemDto;
////import feign.FeignException;
////import org.springframework.cloud.openfeign.FeignClient;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.List;
////
////@FeignClient(name = "item-service", url = "http://localhost:8082")
////public interface ItemServiceClient {
////
////    // FIXED: Use the specific endpoints that actually exist
////    @GetMapping("/api/items/lost/{itemId}")
////    ItemDto getLostItemById(@PathVariable String itemId);
////
////    @GetMapping("/api/items/found/{itemId}")
////    ItemDto getFoundItemById(@PathVariable String itemId);
////
////    // ADDED: Get all items endpoints needed by ClaimedItemService
////    @GetMapping("/api/lost-items")
////    List<ItemDto> getAllLostItems();
////
////    @GetMapping("/api/found-items")
////    List<ItemDto> getAllFoundItems();
////
////    // CRITICAL: Add the missing getOppositeItems method
////    @GetMapping("/api/items/opposite/{itemType}")
////    List<ItemDto> getOppositeItems(@PathVariable String itemType);
////
////    // FIXED: Use the correct deletion endpoints
////    @DeleteMapping("/api/items/lost/{itemId}")
////    ResponseEntity<String> deleteLostItem(@PathVariable String itemId);
////
////    @DeleteMapping("/api/items/found/{itemId}")
////    ResponseEntity<String> deleteFoundItem(@PathVariable String itemId);
////
////    // Fallback method for generic item fetching
////    default ItemDto getItemById(String itemId) {
////        try {
////            return getLostItemById(itemId);
////        } catch (FeignException.NotFound e) {
////            try {
////                return getFoundItemById(itemId);
////            } catch (FeignException.NotFound e2) {
////                throw new RuntimeException("Item not found with ID: " + itemId);
////            }
////        }
////    }
////
////    @GetMapping("/api/items/health")
////    ResponseEntity<String> healthCheck();
////}
//
//
//package edu.icet.ecom.client;
//
//import edu.icet.ecom.model.dto.ItemDto;
//import feign.FeignException;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@FeignClient(name = "item-service", url = "http://localhost:8082")
//public interface ItemServiceClient {
//
//    // FIXED: Use the actual endpoints from your Item Service
//    @GetMapping("/api/items/all")
//    List<ItemDto> getAllItems();
//
//    @GetMapping("/api/items/{id}")
//    ItemDto getItemById(@PathVariable String id);
//
//    // ADDED: Missing endpoints that ClaimedItemService needs
//    @GetMapping("/api/items/lost/{id}")
//    ItemDto getLostItemById(@PathVariable String id);
//
//    @GetMapping("/api/items/found/{id}")
//    ItemDto getFoundItemById(@PathVariable String id);
//
//    @GetMapping("/api/items/opposite/{itemType}")
//    List<ItemDto> getOppositeItems(@PathVariable String itemType);
//
//    // ADDED: Deletion endpoints that ClaimedItemService needs
//    @DeleteMapping("/api/items/lost/{id}")
//    ResponseEntity<String> deleteLostItem(@PathVariable String id);
//
//    @DeleteMapping("/api/items/found/{id}")
//    ResponseEntity<String> deleteFoundItem(@PathVariable String id);
//
//    @GetMapping("/api/items/health")
//    ResponseEntity<String> healthCheck();
//}

package edu.icet.ecom.client;

import edu.icet.ecom.model.dto.ItemDto;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "item-service", url = "http://localhost:8082")
public interface ItemServiceClient {

    // FIXED: Use the actual endpoints from your Item Service
    @GetMapping("/api/items/all")
    List<ItemDto> getAllItems();

    @GetMapping("/api/items/{id}")
    ItemDto getItemById(@PathVariable String id);

    // CRITICAL FIX: Add the missing deletion endpoints that match your Item Service
    @DeleteMapping("/api/items/lost/{id}")
    ResponseEntity<String> deleteLostItem(@PathVariable String id);

    @DeleteMapping("/api/items/found/{id}")
    ResponseEntity<String> deleteFoundItem(@PathVariable String id);

    // REMOVED: The generic deleteItem method that doesn't exist in Item Service
    // @DeleteMapping("/api/items/{id}")
    // ResponseEntity<String> deleteItem(@PathVariable String id);

    @GetMapping("/api/items/health")
    ResponseEntity<String> healthCheck();
}