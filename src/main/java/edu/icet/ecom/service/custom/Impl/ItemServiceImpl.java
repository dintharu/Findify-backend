package edu.icet.ecom.service.custom.Impl;

import edu.icet.ecom.client.UserServiceClient;
import edu.icet.ecom.model.dto.ItemDto;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import edu.icet.ecom.model.dto.response.UserResponse;
import edu.icet.ecom.service.custom.FoundItemService;
import edu.icet.ecom.service.custom.ItemService;
import edu.icet.ecom.service.custom.LostItemService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final UserServiceClient userServiceClient;

    @Override
    public List<ItemDto> getAllItems() {
        try {
            log.info("Fetching all items (lost and found)");
            List<ItemDto> allItems = new ArrayList<>();

            // Get all lost items
            try {
                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
                log.info("Found {} lost items", lostItems.size());

                // Convert lost items to ItemDto
                for (LostItemResponse lostItem : lostItems) {
                    try {
                        String userEmail = getUserEmailWithFallback(lostItem.getReportedByUserId());
                        ItemDto itemDto = convertLostItemToDto(lostItem, userEmail);
                        allItems.add(itemDto);
                        log.debug("Added lost item: {} (ID: {})", itemDto.getTitle(), itemDto.getId());
                    } catch (Exception e) {
                        log.warn("Error converting lost item ID {}: {}", lostItem.getId(), e.getMessage());
                        // Still add the item with a placeholder email
                        try {
                            ItemDto itemDto = convertLostItemToDto(lostItem, "unknown@example.com");
                            allItems.add(itemDto);
                        } catch (Exception ex) {
                            log.error("Failed to add lost item even with fallback: {}", ex.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching lost items: {}", e.getMessage());
            }

            // Get all found items
            try {
                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
                log.info("Found {} found items", foundItems.size());

                // Convert found items to ItemDto
                for (FoundItemResponse foundItem : foundItems) {
                    try {
                        String userEmail = getUserEmailWithFallback(foundItem.getFoundByUserId());
                        ItemDto itemDto = convertFoundItemToDto(foundItem, userEmail);
                        allItems.add(itemDto);
                        log.debug("Added found item: {} (ID: {})", itemDto.getTitle(), itemDto.getId());
                    } catch (Exception e) {
                        log.warn("Error converting found item ID {}: {}", foundItem.getId(), e.getMessage());
                        // Still add the item with a placeholder email
                        try {
                            ItemDto itemDto = convertFoundItemToDto(foundItem, "unknown@example.com");
                            allItems.add(itemDto);
                        } catch (Exception ex) {
                            log.error("Failed to add found item even with fallback: {}", ex.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching found items: {}", e.getMessage());
            }

            log.info("Returning {} total items", allItems.size());

            // Debug: Log some details about the items
            allItems.forEach(item -> {
                log.debug("Item: {} | Type: {} | Image URL: {} | User: {}",
                        item.getTitle(), item.getItemType(),
                        item.getImageUrl() != null ? "Present" : "NULL",
                        item.getUserEmail());
            });

            return allItems;

        } catch (Exception e) {
            log.error("Error fetching all items: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ItemDto getItemById(String id) {
        try {
            Long itemId = Long.parseLong(id);
            log.debug("Searching for item with ID: {}", itemId);

            // Try to find in lost items first
            try {
                LostItemResponse lostItem = lostItemService.getLostItemById(itemId);
                if (lostItem != null) {
                    log.debug("Found item {} in lost items", itemId);
                    String userEmail = getUserEmailWithFallback(lostItem.getReportedByUserId());
                    return convertLostItemToDto(lostItem, userEmail);
                }
            } catch (Exception e) {
                log.debug("Item {} not found in lost items: {}", itemId, e.getMessage());
            }

            // Try to find in found items
            try {
                FoundItemResponse foundItem = foundItemService.getFoundItemById(itemId);
                if (foundItem != null) {
                    log.debug("Found item {} in found items", itemId);
                    String userEmail = getUserEmailWithFallback(foundItem.getFoundByUserId());
                    return convertFoundItemToDto(foundItem, userEmail);
                }
            } catch (Exception e) {
                log.debug("Item {} not found in found items: {}", itemId, e.getMessage());
            }

            log.warn("Item with ID {} not found anywhere", itemId);
            return null;

        } catch (NumberFormatException e) {
            log.error("Invalid item ID format: {}", id);
            return null;
        } catch (Exception e) {
            log.error("Error fetching item by ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<ItemDto> getOppositeItems(String itemType) {
        try {
            log.info("Fetching opposite items for type: {}", itemType);

            if ("LOST".equalsIgnoreCase(itemType)) {
                // If requesting opposite of LOST, return FOUND items
                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
                log.debug("Converting {} found items", foundItems.size());

                return foundItems.stream()
                        .map(foundItem -> {
                            try {
                                String userEmail = getUserEmailWithFallback(foundItem.getFoundByUserId());
                                return convertFoundItemToDto(foundItem, userEmail);
                            } catch (Exception e) {
                                log.warn("Error converting found item {}: {}", foundItem.getId(), e.getMessage());
                                try {
                                    return convertFoundItemToDto(foundItem, "unknown@example.com");
                                } catch (Exception ex) {
                                    log.error("Failed to convert found item with fallback: {}", ex.getMessage());
                                    return null;
                                }
                            }
                        })
                        .filter(item -> item != null) // Remove null entries
                        .collect(Collectors.toList());

            } else if ("FOUND".equalsIgnoreCase(itemType)) {
                // If requesting opposite of FOUND, return LOST items
                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
                log.debug("Converting {} lost items", lostItems.size());

                return lostItems.stream()
                        .map(lostItem -> {
                            try {
                                String userEmail = getUserEmailWithFallback(lostItem.getReportedByUserId());
                                return convertLostItemToDto(lostItem, userEmail);
                            } catch (Exception e) {
                                log.warn("Error converting lost item {}: {}", lostItem.getId(), e.getMessage());
                                try {
                                    return convertLostItemToDto(lostItem, "unknown@example.com");
                                } catch (Exception ex) {
                                    log.error("Failed to convert lost item with fallback: {}", ex.getMessage());
                                    return null;
                                }
                            }
                        })
                        .filter(item -> item != null) // Remove null entries
                        .collect(Collectors.toList());
            } else {
                log.warn("Invalid item type requested: {}", itemType);
                return new ArrayList<>();
            }

        } catch (Exception e) {
            log.error("Error fetching opposite items for type {}: {}", itemType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private String getUserEmailWithFallback(Long userId) {
        try {
            log.debug("Fetching user email for userId: {}", userId);

            UserResponse user = userServiceClient.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                log.debug("Successfully retrieved email for userId {}: {}", userId, user.getEmail());
                return user.getEmail();
            } else {
                log.warn("User found but email is null for userId: {}", userId);
                return "user" + userId + "@example.com"; // Fallback email
            }

        } catch (FeignException e) {
            log.error("Feign error getting user email for userId {}: Status {}, Message: {}",
                    userId, e.status(), e.getMessage());
            return "user" + userId + "@example.com"; // Fallback email
        } catch (Exception e) {
            log.error("Error getting user email for userId {}: {}", userId, e.getMessage(), e);
            return "user" + userId + "@example.com"; // Fallback email
        }
    }

    // Keep the old method for compatibility but mark as deprecated
    @Deprecated
    private String getUserEmail(Long userId) {
        return getUserEmailWithFallback(userId);
    }

    private ItemDto convertLostItemToDto(LostItemResponse lostItem, String userEmail) {
        try {
            ItemDto dto = new ItemDto();
            dto.setId(lostItem.getId().toString());
            dto.setTitle(lostItem.getName());
            dto.setDescription(lostItem.getDescription());
            dto.setCategory(lostItem.getCategoryName());
            dto.setImageUrl(lostItem.getImageUrl());
            dto.setUserId(lostItem.getReportedByUserId().toString());
            dto.setUserEmail(userEmail);
            dto.setItemType("LOST");
            dto.setLocation(lostItem.getLocation());
            dto.setCreatedAt(lostItem.getReportedDate());

            log.debug("Converted lost item: {} (ID: {}) for user: {} | Image URL: {}",
                    dto.getTitle(), dto.getId(), dto.getUserEmail(),
                    dto.getImageUrl() != null ? "Present" : "NULL");
            return dto;
        } catch (Exception e) {
            log.error("Error converting lost item to DTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ItemDto convertFoundItemToDto(FoundItemResponse foundItem, String userEmail) {
        try {
            ItemDto dto = new ItemDto();
            dto.setId(foundItem.getId().toString());
            dto.setTitle(foundItem.getName());
            dto.setDescription(foundItem.getDescription());
            dto.setCategory(foundItem.getCategoryName());
            dto.setImageUrl(foundItem.getImageUrl());
            dto.setUserId(foundItem.getFoundByUserId().toString());
            dto.setUserEmail(userEmail);
            dto.setItemType("FOUND");
            dto.setLocation(foundItem.getLocation());
            dto.setCreatedAt(foundItem.getReportedDate());

            log.debug("Converted found item: {} (ID: {}) for user: {} | Image URL: {}",
                    dto.getTitle(), dto.getId(), dto.getUserEmail(),
                    dto.getImageUrl() != null ? "Present" : "NULL");
            return dto;
        } catch (Exception e) {
            log.error("Error converting found item to DTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}