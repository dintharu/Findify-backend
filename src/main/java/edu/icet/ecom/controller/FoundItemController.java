package edu.icet.ecom.controller;


import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.dto.response.LostItemResponse;
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
@CrossOrigin
public class FoundItemController {

    private final FoundItemService foundItemService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<FoundItemResponse> createFoundItem(
            @PathVariable Long userId,
            @Valid @RequestBody CreateFoundItemRequest request) {
        FoundItemResponse response = foundItemService.createFoundItem(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoundItemResponse> getFoundItemById(@PathVariable Long id) {
        FoundItemResponse foundItemResponse = foundItemService.getFoundItemById(id);
        return ResponseEntity.ok(foundItemResponse);
    }

    @GetMapping
    public ResponseEntity<List<FoundItemResponse>> getAllFoundItems(Pageable pageable) {
        List<FoundItemResponse> foundItemResponses = foundItemService.getAllFoundItems(pageable);
        return ResponseEntity.ok(foundItemResponses);
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<FoundItemResponse> updateFoundItem(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody CreateFoundItemRequest request) {
        FoundItemResponse updatedItem = foundItemService.updateFoundItem(id, request, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoundItemResponse>> searchFoundItems(
            @RequestParam String query,
            Pageable pageable) {
        List<FoundItemResponse> searchResults = foundItemService.searchFoundItems(query, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoundItem(@PathVariable Long id) {
        foundItemService.deleteFoundItem(id);
        return ResponseEntity.noContent().build();
    }
}
