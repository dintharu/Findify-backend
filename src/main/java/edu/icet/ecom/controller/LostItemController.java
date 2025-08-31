package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import edu.icet.ecom.service.custom.LostItemService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
@CrossOrigin
public class LostItemController {

    private final LostItemService lostItemService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<LostItemResponse> createLostItem(
            @PathVariable Long userId,
            @Valid @RequestBody CreateLostItemRequest request) {
        LostItemResponse response = lostItemService.createLostItem(request, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LostItemResponse> getLostItemById(@PathVariable Long id) {
        LostItemResponse lostItem = lostItemService.getLostItemById(id);
        return ResponseEntity.ok(lostItem);
    }

    @GetMapping
    public ResponseEntity<List<LostItemResponse>> getAllLostItems(Pageable pageable) {
        List<LostItemResponse> lostItems = lostItemService.getAllLostItems(pageable);
        return ResponseEntity.ok(lostItems);
    }

    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<LostItemResponse> updateLostItem(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody CreateLostItemRequest request) {
        LostItemResponse updatedItem = lostItemService.updateLostItem(id, request, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LostItemResponse>> searchLostItems(
            @RequestParam String query,
            Pageable pageable) {
        List<LostItemResponse> searchResults = lostItemService.searchLostItems(query, pageable);
        return ResponseEntity.ok(searchResults);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLostItem(@PathVariable Long id) {
        lostItemService.deleteLostItem(id);
        return ResponseEntity.noContent().build();
    }
}
