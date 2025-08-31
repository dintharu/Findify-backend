package edu.icet.ecom.service.custom.Impl;
import edu.icet.ecom.service.custom.ImageSimilarityIntegrationService;
import edu.icet.ecom.enums.ItemStatus;
import edu.icet.ecom.exception.ItemNotFoundException;
import edu.icet.ecom.mapper.FoundItemMapper;
import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.entity.Category;
import edu.icet.ecom.model.entity.FoundItems;
import edu.icet.ecom.repository.CategoryRepository;
import edu.icet.ecom.repository.FoundItemRepository;
import edu.icet.ecom.service.custom.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FoundItemServiceImpl implements FoundItemService {

    private final ImageSimilarityIntegrationService imageSimilarityIntegrationService;
    private final FoundItemMapper foundItemMapper;
    private final FoundItemRepository foundItemRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public FoundItemResponse createFoundItem(CreateFoundItemRequest request, Long userId) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ItemNotFoundException("Category not found"));

        FoundItems foundItems = foundItemMapper.toEntity(request);
        foundItems.setFoundByUserId(userId);
        foundItems.setCategory(category);
        foundItems.setStatus(ItemStatus.ACTIVE);
        foundItems.setReportedDate(LocalDateTime.now());

        FoundItems savedItem = foundItemRepository.save(foundItems);
        FoundItemResponse response = foundItemMapper.toResponse(savedItem);

        // Trigger image similarity check
        imageSimilarityIntegrationService.checkForMatchesAfterFoundItemCreated(response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public FoundItemResponse getFoundItemById(Long id) {
        FoundItems foundItems = foundItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Found item not found with id: " + id));

        return foundItemMapper.toResponse(foundItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoundItemResponse> getAllFoundItems(Pageable pageable) {
        return foundItemRepository.findAll(pageable).getContent()
                .stream()
                .map(foundItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFoundItem(Long id) {
        if (!foundItemRepository.existsById(id)) {
            throw new ItemNotFoundException("Found item not found with ID: " + id);
        }
        foundItemRepository.deleteById(id);
    }

    @Override
    public FoundItemResponse updateFoundItem(Long id, CreateFoundItemRequest request, Long userId) {
        // Find existing item
        FoundItems existingItem = foundItemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Found item not found with ID: " + id));

        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ItemNotFoundException("Category not found with ID: " + request.getCategoryId()));

        // Update the existing item
        existingItem.setName(request.getName());
        existingItem.setDescription(request.getDescription());
        existingItem.setLocation(request.getLocation());
        existingItem.setFoundDate(request.getFoundDate());
        existingItem.setCategory(category);
        if (request.getImageUrl() != null) {
            existingItem.setImageUrl(request.getImageUrl());
        }

        // Save updated item
        FoundItems updatedItem = foundItemRepository.save(existingItem);
        return foundItemMapper.toResponse(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoundItemResponse> searchFoundItems(String query, Pageable pageable) {
        // Use the repository method to search by name
        List<FoundItems> searchResults = foundItemRepository.findByNameContainingIgnoreCase(query);

        // Apply pagination manually since the repository method doesn't support Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), searchResults.size());

        if (start >= searchResults.size()) {
            return List.of(); // Return empty list if start is beyond results
        }

        return searchResults.subList(start, end)
                .stream()
                .map(foundItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}