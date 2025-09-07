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
//    @GetMapping("/api/items/all")
//    List<ItemDto> getAllItems();
//
//    @GetMapping("/api/items/{id}")
//    ItemDto getItemById(@PathVariable String id);
//
//    @GetMapping("/api/items/opposite/{itemType}")
//    List<ItemDto> getOppositeItems(@PathVariable String itemType);
//
//    // New endpoints needed for claim functionality
//    @DeleteMapping("/api/items/{id}")
//    ResponseEntity<String> deleteItem(@PathVariable String id);
//
//    @PutMapping("/api/items/{id}/status")
//    ResponseEntity<String> updateItemStatus(
//            @PathVariable String id,
//            @RequestParam String status);
//
//    @GetMapping("/api/items/user/{userEmail}")
//    List<ItemDto> getItemsByUserEmail(@PathVariable String userEmail);
//
//    @PostMapping("/api/items/batch-delete")
//    ResponseEntity<String> deleteItemsBatch(@RequestBody List<String> itemIds);
//}



//}

package edu.icet.ecom.client;

import edu.icet.ecom.model.dto.ItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "item-service", url = "http://localhost:8082")
public interface ItemServiceClient {

    // FIXED: Use the correct endpoint that exists in your ItemService
    @GetMapping("/api/items/all")
    List<ItemDto> getAllItems();

    @GetMapping("/api/items/{id}")
    ItemDto getItemById(@PathVariable String id);

    @GetMapping("/api/items/health")
    ResponseEntity<String> healthCheck();
}