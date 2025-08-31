package edu.icet.ecom.service.custom.Impl;
import edu.icet.ecom.service.custom.ImageSimilarityIntegrationService;
import edu.icet.ecom.enums.ItemStatus;
import edu.icet.ecom.exception.ItemNotFoundException;
import edu.icet.ecom.mapper.LostItemMapper;
import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import edu.icet.ecom.model.entity.Category;
import edu.icet.ecom.model.entity.LostItem;
import edu.icet.ecom.repository.CategoryRepository;
import edu.icet.ecom.repository.LostItemRepository;
import edu.icet.ecom.service.custom.LostItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LostItemServiceImpl implements LostItemService {

    private final ImageSimilarityIntegrationService imageSimilarityIntegrationService;
    private final LostItemRepository lostItemRepository;
    private final CategoryRepository categoryRepository;
    private final LostItemMapper lostItemMapper;

    // Remove the manual constructor - @RequiredArgsConstructor handles this

    @Override
    public LostItemResponse createLostItem(CreateLostItemRequest request, Long userId) {
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ItemNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Convert request to entity
        LostItem lostItem = lostItemMapper.toEntity(request);
        lostItem.setReportedByUserId(userId);
        lostItem.setCategory(category);
        lostItem.setStatus(ItemStatus.ACTIVE);
        lostItem.setReportedDate(LocalDateTime.now());

        // Save to database
        LostItem savedItem = lostItemRepository.save(lostItem);

        // Convert to response and return
        LostItemResponse response = lostItemMapper.toResponse(savedItem);

        // Trigger image similarity check
        imageSimilarityIntegrationService.checkForMatchesAfterLostItemCreated(response);

        return response;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public LostItemResponse getLostItemById(Long id) {
        LostItem lostItem = lostItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Lost item not found with ID: " + id));
        return lostItemMapper.toResponse(lostItem);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<LostItemResponse> getAllLostItems(Pageable pageable) {
        Page<LostItem> lostItemPage = lostItemRepository.findAll(pageable);
        return lostItemPage.getContent()
                .stream()
                .map(lostItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLostItem(Long id) {
        if (!lostItemRepository.existsById(id)) {
            throw new ItemNotFoundException("Lost item not found with ID: " + id);
        }
        lostItemRepository.deleteById(id);
    }

    @Override
    public LostItemResponse updateLostItem(Long id, CreateLostItemRequest request, Long userId) {
        // Find existing item
        LostItem existingItem = lostItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Lost item not found with ID: " + id));

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ItemNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Update the existing item
        existingItem.setName(request.getName());
        existingItem.setDescription(request.getDescription());
        existingItem.setLocation(request.getLocation());
        existingItem.setLostDate(request.getLostDate());
        existingItem.setCategory(category);
        if (request.getImageUrl() != null) {
            existingItem.setImageUrl(request.getImageUrl());
        }

        // Save updated item
        LostItem updatedItem = lostItemRepository.save(existingItem);
        return lostItemMapper.toResponse(updatedItem);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<LostItemResponse> searchLostItems(String query, Pageable pageable) {
        // Use the repository method to search by name
        List<LostItem> searchResults = lostItemRepository.findByNameContainingIgnoreCase(query);

        // Apply pagination manually since the repository method doesn't support Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), searchResults.size());

        if (start >= searchResults.size()) {
            return List.of(); // Return empty list if start is beyond results
        }

        return searchResults.subList(start, end)
                .stream()
                .map(lostItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}