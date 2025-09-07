//package edu.icet.ecom.controller;
//
//
//import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
//import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
//import edu.icet.ecom.model.dto.response.FoundItemResponse;
//import edu.icet.ecom.model.dto.response.LostItemResponse;
//import edu.icet.ecom.service.custom.FoundItemService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/found-items")
//@RequiredArgsConstructor
//@CrossOrigin
//public class FoundItemController {
//
//    private final FoundItemService foundItemService;
//
//    @PostMapping("/user/{userId}")
//    public ResponseEntity<FoundItemResponse> createFoundItem(
//            @PathVariable Long userId,
//            @Valid @RequestBody CreateFoundItemRequest request) {
//        FoundItemResponse response = foundItemService.createFoundItem(request, userId);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<FoundItemResponse> getFoundItemById(@PathVariable Long id) {
//        FoundItemResponse foundItemResponse = foundItemService.getFoundItemById(id);
//        return ResponseEntity.ok(foundItemResponse);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<FoundItemResponse>> getAllFoundItems(Pageable pageable) {
//        List<FoundItemResponse> foundItemResponses = foundItemService.getAllFoundItems(pageable);
//        return ResponseEntity.ok(foundItemResponses);
//    }
//
//    @PutMapping("/{id}/user/{userId}")
//    public ResponseEntity<FoundItemResponse> updateFoundItem(
//            @PathVariable Long id,
//            @PathVariable Long userId,
//            @Valid @RequestBody CreateFoundItemRequest request) {
//        FoundItemResponse updatedItem = foundItemService.updateFoundItem(id, request, userId);
//        return ResponseEntity.ok(updatedItem);
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<List<FoundItemResponse>> searchFoundItems(
//            @RequestParam String query,
//            Pageable pageable) {
//        List<FoundItemResponse> searchResults = foundItemService.searchFoundItems(query, pageable);
//        return ResponseEntity.ok(searchResults);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteFoundItem(@PathVariable Long id) {
//        foundItemService.deleteFoundItem(id);
//        return ResponseEntity.noContent().build();
//    }
//}


//// FoundItemController.java - FIXED CORS
//package edu.icet.ecom.controller;
//
//import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
//import edu.icet.ecom.model.dto.response.FoundItemResponse;
//import edu.icet.ecom.service.custom.FoundItemService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/found-items")
//@RequiredArgsConstructor
//@CrossOrigin(
//        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"},
//        allowCredentials = "true",
//        allowedHeaders = {"*"},
//        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
//)
//public class FoundItemController {
//
//    private final FoundItemService foundItemService;
//
//    @PostMapping("/user/{userId}")
//    public ResponseEntity<FoundItemResponse> createFoundItem(
//            @PathVariable Long userId,
//            @Valid @RequestBody CreateFoundItemRequest request) {
//        FoundItemResponse response = foundItemService.createFoundItem(request, userId);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<FoundItemResponse> getFoundItemById(@PathVariable Long id) {
//        FoundItemResponse foundItemResponse = foundItemService.getFoundItemById(id);
//        return ResponseEntity.ok(foundItemResponse);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<FoundItemResponse>> getAllFoundItems(Pageable pageable) {
//        List<FoundItemResponse> foundItemResponses = foundItemService.getAllFoundItems(pageable);
//        return ResponseEntity.ok(foundItemResponses);
//    }
//
//    @PutMapping("/{id}/user/{userId}")
//    public ResponseEntity<FoundItemResponse> updateFoundItem(
//            @PathVariable Long id,
//            @PathVariable Long userId,
//            @Valid @RequestBody CreateFoundItemRequest request) {
//        FoundItemResponse updatedItem = foundItemService.updateFoundItem(id, request, userId);
//        return ResponseEntity.ok(updatedItem);
//    }
//
//    @GetMapping("/search")
//    public ResponseEntity<List<FoundItemResponse>> searchFoundItems(
//            @RequestParam String query,
//            Pageable pageable) {
//        List<FoundItemResponse> searchResults = foundItemService.searchFoundItems(query, pageable);
//        return ResponseEntity.ok(searchResults);
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteFoundItem(@PathVariable Long id) {
//        foundItemService.deleteFoundItem(id);
//        return ResponseEntity.noContent().build();
//    }
//}


// FIXED: FoundItemController.java - Accept numeric IDs
package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.service.custom.FoundItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/found-items")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"},
        allowCredentials = "true",
        allowedHeaders = {"*"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class FoundItemController {

    private final FoundItemService foundItemService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<FoundItemResponse> createFoundItem(
            @PathVariable Long userId,
            @Valid @RequestBody CreateFoundItemRequest request) {
        FoundItemResponse response = foundItemService.createFoundItem(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // FIXED: Accept both Long and String IDs for compatibility
    @GetMapping("/{id}")
    public ResponseEntity<FoundItemResponse> getFoundItemById(@PathVariable String id) {
        try {
            Long itemId = Long.parseLong(id);
            FoundItemResponse foundItemResponse = foundItemService.getFoundItemById(itemId);
            return ResponseEntity.ok(foundItemResponse);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FoundItemResponse>> getAllFoundItems(Pageable pageable) {
        List<FoundItemResponse> foundItemResponses = foundItemService.getAllFoundItems(pageable);
        return ResponseEntity.ok(foundItemResponses);
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<FoundItemResponse> updateFoundItem(
            @PathVariable String id,
            @PathVariable Long userId,
            @Valid @RequestBody CreateFoundItemRequest request) {
        try {
            Long itemId = Long.parseLong(id);
            FoundItemResponse updatedItem = foundItemService.updateFoundItem(itemId, request, userId);
            return ResponseEntity.ok(updatedItem);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoundItemResponse>> searchFoundItems(
            @RequestParam String query,
            Pageable pageable) {
        List<FoundItemResponse> searchResults = foundItemService.searchFoundItems(query, pageable);
        return ResponseEntity.ok(searchResults);
    }

    // FIXED: Accept String ID and convert to Long
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoundItem(@PathVariable String id) {
        try {
            Long itemId = Long.parseLong(id);
            foundItemService.deleteFoundItem(itemId);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}