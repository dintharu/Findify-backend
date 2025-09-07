//package edu.icet.ecom.service.custom.Impl;
//
//import edu.icet.ecom.client.UserServiceClient;
//import edu.icet.ecom.model.dto.ContactInfo;
//import edu.icet.ecom.model.dto.ItemDto;
//import edu.icet.ecom.model.dto.response.FoundItemResponse;
//import edu.icet.ecom.model.dto.response.LostItemResponse;
//import edu.icet.ecom.model.dto.response.UserResponse;
//import edu.icet.ecom.service.custom.FoundItemService;
//import edu.icet.ecom.service.custom.ItemService;
//import edu.icet.ecom.service.custom.LostItemService;
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ItemServiceImpl implements ItemService {
//
//    private final LostItemService lostItemService;
//    private final FoundItemService foundItemService;
//    private final UserServiceClient userServiceClient;
//
//    @Override
//    public List<ItemDto> getAllItems() {
//        try {
//            log.info("Fetching all items (lost and found)");
//            List<ItemDto> allItems = new ArrayList<>();
//
//            // Get all lost items
//            try {
//                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
//                log.info("Found {} lost items", lostItems.size());
//
//                // Convert lost items to ItemDto
//                for (LostItemResponse lostItem : lostItems) {
//                    try {
//                        String userEmail = getUserEmailWithFallback(lostItem.getReportedByUserId());
//                        ItemDto itemDto = convertLostItemToDto(lostItem, userEmail);
//                        allItems.add(itemDto);
//                        log.debug("Added lost item: {} (ID: {})", itemDto.getTitle(), itemDto.getId());
//                    } catch (Exception e) {
//                        log.warn("Error converting lost item ID {}: {}", lostItem.getId(), e.getMessage());
//                        // Still add the item with a placeholder email
//                        try {
//                            ItemDto itemDto = convertLostItemToDto(lostItem, "unknown@example.com");
//                            allItems.add(itemDto);
//                        } catch (Exception ex) {
//                            log.error("Failed to add lost item even with fallback: {}", ex.getMessage());
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error fetching lost items: {}", e.getMessage());
//            }
//
//            // Get all found items
//            try {
//                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
//                log.info("Found {} found items", foundItems.size());
//
//                // Convert found items to ItemDto
//                for (FoundItemResponse foundItem : foundItems) {
//                    try {
//                        String userEmail = getUserEmailWithFallback(foundItem.getFoundByUserId());
//                        ItemDto itemDto = convertFoundItemToDto(foundItem, userEmail);
//                        allItems.add(itemDto);
//                        log.debug("Added found item: {} (ID: {})", itemDto.getTitle(), itemDto.getId());
//                    } catch (Exception e) {
//                        log.warn("Error converting found item ID {}: {}", foundItem.getId(), e.getMessage());
//                        // Still add the item with a placeholder email
//                        try {
//                            ItemDto itemDto = convertFoundItemToDto(foundItem, "unknown@example.com");
//                            allItems.add(itemDto);
//                        } catch (Exception ex) {
//                            log.error("Failed to add found item even with fallback: {}", ex.getMessage());
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error fetching found items: {}", e.getMessage());
//            }
//
//            log.info("Returning {} total items", allItems.size());
//
//            // Debug: Log some details about the items
//            allItems.forEach(item -> {
//                log.debug("Item: {} | Type: {} | Image URL: {} | User: {}",
//                        item.getTitle(), item.getItemType(),
//                        item.getImageUrl() != null ? "Present" : "NULL",
//                        item.getUserEmail());
//            });
//
//            return allItems;
//
//        } catch (Exception e) {
//            log.error("Error fetching all items: {}", e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    @Override
//    public ItemDto getItemById(String id) {
//        try {
//            Long itemId = Long.parseLong(id);
//            log.debug("Searching for item with ID: {}", itemId);
//
//            // Try to find in lost items first
//            try {
//                LostItemResponse lostItem = lostItemService.getLostItemById(itemId);
//                if (lostItem != null) {
//                    log.debug("Found item {} in lost items", itemId);
//                    String userEmail = getUserEmailWithFallback(lostItem.getReportedByUserId());
//                    return convertLostItemToDto(lostItem, userEmail);
//                }
//            } catch (Exception e) {
//                log.debug("Item {} not found in lost items: {}", itemId, e.getMessage());
//            }
//
//            // Try to find in found items
//            try {
//                FoundItemResponse foundItem = foundItemService.getFoundItemById(itemId);
//                if (foundItem != null) {
//                    log.debug("Found item {} in found items", itemId);
//                    String userEmail = getUserEmailWithFallback(foundItem.getFoundByUserId());
//                    return convertFoundItemToDto(foundItem, userEmail);
//                }
//            } catch (Exception e) {
//                log.debug("Item {} not found in found items: {}", itemId, e.getMessage());
//            }
//
//            log.warn("Item with ID {} not found anywhere", itemId);
//            return null;
//
//        } catch (NumberFormatException e) {
//            log.error("Invalid item ID format: {}", id);
//            return null;
//        } catch (Exception e) {
//            log.error("Error fetching item by ID {}: {}", id, e.getMessage(), e);
//            return null;
//        }
//    }
//
//    @Override
//    public List<ItemDto> getOppositeItems(String itemType) {
//        try {
//            log.info("Fetching opposite items for type: {}", itemType);
//
//            if ("LOST".equalsIgnoreCase(itemType)) {
//                // If requesting opposite of LOST, return FOUND items
//                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
//                log.debug("Converting {} found items", foundItems.size());
//
//                return foundItems.stream()
//                        .map(foundItem -> {
//                            try {
//                                String userEmail = getUserEmailWithFallback(foundItem.getFoundByUserId());
//                                return convertFoundItemToDto(foundItem, userEmail);
//                            } catch (Exception e) {
//                                log.warn("Error converting found item {}: {}", foundItem.getId(), e.getMessage());
//                                try {
//                                    return convertFoundItemToDto(foundItem, "unknown@example.com");
//                                } catch (Exception ex) {
//                                    log.error("Failed to convert found item with fallback: {}", ex.getMessage());
//                                    return null;
//                                }
//                            }
//                        })
//                        .filter(item -> item != null) // Remove null entries
//                        .collect(Collectors.toList());
//
//            } else if ("FOUND".equalsIgnoreCase(itemType)) {
//                // If requesting opposite of FOUND, return LOST items
//                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
//                log.debug("Converting {} lost items", lostItems.size());
//
//                return lostItems.stream()
//                        .map(lostItem -> {
//                            try {
//                                String userEmail = getUserEmailWithFallback(lostItem.getReportedByUserId());
//                                return convertLostItemToDto(lostItem, userEmail);
//                            } catch (Exception e) {
//                                log.warn("Error converting lost item {}: {}", lostItem.getId(), e.getMessage());
//                                try {
//                                    return convertLostItemToDto(lostItem, "unknown@example.com");
//                                } catch (Exception ex) {
//                                    log.error("Failed to convert lost item with fallback: {}", ex.getMessage());
//                                    return null;
//                                }
//                            }
//                        })
//                        .filter(item -> item != null) // Remove null entries
//                        .collect(Collectors.toList());
//            } else {
//                log.warn("Invalid item type requested: {}", itemType);
//                return new ArrayList<>();
//            }
//
//        } catch (Exception e) {
//            log.error("Error fetching opposite items for type {}: {}", itemType, e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    private String getUserEmailWithFallback(Long userId) {
//        try {
//            log.debug("Fetching user email for userId: {}", userId);
//
//            UserResponse user = userServiceClient.getUserById(userId);
//            if (user != null && user.getEmail() != null) {
//                log.debug("Successfully retrieved email for userId {}: {}", userId, user.getEmail());
//                return user.getEmail();
//            } else {
//                log.warn("User found but email is null for userId: {}", userId);
//                return "user" + userId + "@example.com"; // Fallback email
//            }
//
//        } catch (FeignException e) {
//            log.error("Feign error getting user email for userId {}: Status {}, Message: {}",
//                    userId, e.status(), e.getMessage());
//            return "user" + userId + "@example.com"; // Fallback email
//        } catch (Exception e) {
//            log.error("Error getting user email for userId {}: {}", userId, e.getMessage(), e);
//            return "user" + userId + "@example.com"; // Fallback email
//        }
//    }
//
//    // Keep the old method for compatibility but mark as deprecated
//    @Deprecated
//    private String getUserEmail(Long userId) {
//        return getUserEmailWithFallback(userId);
//    }
//
//    private ItemDto convertLostItemToDto(LostItemResponse lostItem, String userEmail) {
//        try {
//            ItemDto dto = new ItemDto();
//            dto.setId(lostItem.getId().toString());
//            dto.setTitle(lostItem.getName());
//            dto.setDescription(lostItem.getDescription());
//            dto.setCategory(lostItem.getCategoryName());
//            dto.setImageUrl(lostItem.getImageUrl());
//            dto.setUserId(lostItem.getReportedByUserId().toString());
//            dto.setUserEmail(userEmail);
//            dto.setItemType("LOST");
//            dto.setLocation(lostItem.getLocation());
//            dto.setCreatedAt(lostItem.getReportedDate());
//
//            // CRITICAL FIX - Add contact info with real user data
//            try {
//                UserResponse user = userServiceClient.getUserById(lostItem.getReportedByUserId());
//                if (user != null) {
//                    ContactInfo contactInfo = new ContactInfo();
//                    contactInfo.setId(user.getId()); // This is the key - real user ID
//                    contactInfo.setName(user.getFullName());
//                    contactInfo.setEmail(user.getEmail());
//                    contactInfo.setPhone(""); // Add phone if available
//                    dto.setContactInfo(contactInfo);
//
//                    log.debug("Set contact info for lost item {}: User ID={}, Name={}, Email={}",
//                            dto.getId(), contactInfo.getId(), contactInfo.getName(), contactInfo.getEmail());
//                }
//            } catch (Exception e) {
//                log.warn("Could not fetch user info for lost item contact: {}", e.getMessage());
//
//                // Fallback contact info
//                ContactInfo fallbackContact = new ContactInfo();
//                fallbackContact.setId(lostItem.getReportedByUserId());
//                fallbackContact.setName("User " + lostItem.getReportedByUserId());
//                fallbackContact.setEmail(userEmail);
//                fallbackContact.setPhone("");
//                dto.setContactInfo(fallbackContact);
//            }
//
//            return dto;
//        } catch (Exception e) {
//            log.error("Error converting lost item to DTO: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//
//    // Do the same for convertFoundItemToDto
//    private ItemDto convertFoundItemToDto(FoundItemResponse foundItem, String userEmail) {
//        try {
//            ItemDto dto = new ItemDto();
//            dto.setId(foundItem.getId().toString());
//            dto.setTitle(foundItem.getName());
//            dto.setDescription(foundItem.getDescription());
//            dto.setCategory(foundItem.getCategoryName());
//            dto.setImageUrl(foundItem.getImageUrl());
//            dto.setUserId(foundItem.getFoundByUserId().toString());
//            dto.setUserEmail(userEmail);
//            dto.setItemType("FOUND");
//            dto.setLocation(foundItem.getLocation());
//            dto.setCreatedAt(foundItem.getReportedDate());
//
//            // CRITICAL FIX - Add contact info with real user data
//            try {
//                UserResponse user = userServiceClient.getUserById(foundItem.getFoundByUserId());
//                if (user != null) {
//                    ContactInfo contactInfo = new ContactInfo();
//                    contactInfo.setId(user.getId()); // This is the key - real user ID
//                    contactInfo.setName(user.getFullName());
//                    contactInfo.setEmail(user.getEmail());
//                    contactInfo.setPhone(""); // Add phone if available
//                    dto.setContactInfo(contactInfo);
//
//                    log.debug("Set contact info for found item {}: User ID={}, Name={}, Email={}",
//                            dto.getId(), contactInfo.getId(), contactInfo.getName(), contactInfo.getEmail());
//                }
//            } catch (Exception e) {
//                log.warn("Could not fetch user info for found item contact: {}", e.getMessage());
//
//                // Fallback contact info
//                ContactInfo fallbackContact = new ContactInfo();
//                fallbackContact.setId(foundItem.getFoundByUserId());
//                fallbackContact.setName("User " + foundItem.getFoundByUserId());
//                fallbackContact.setEmail(userEmail);
//                fallbackContact.setPhone("");
//                dto.setContactInfo(fallbackContact);
//            }
//
//            return dto;
//        } catch (Exception e) {
//            log.error("Error converting found item to DTO: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//}

//
//package edu.icet.ecom.service.custom.Impl;
//
//import edu.icet.ecom.client.UserServiceClient;
//import edu.icet.ecom.model.dto.ContactInfo;
//import edu.icet.ecom.model.dto.ItemDto;
//import edu.icet.ecom.model.dto.response.FoundItemResponse;
//import edu.icet.ecom.model.dto.response.LostItemResponse;
//import edu.icet.ecom.model.dto.response.UserResponse;
//import edu.icet.ecom.service.custom.FoundItemService;
//import edu.icet.ecom.service.custom.ItemService;
//import edu.icet.ecom.service.custom.LostItemService;
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ItemServiceImpl implements ItemService {
//
//    private final LostItemService lostItemService;
//    private final FoundItemService foundItemService;
//    private final UserServiceClient userServiceClient;
//
//    @Override
//    public List<ItemDto> getAllItems() {
//        try {
//            log.info("Fetching all items (lost and found)");
//            List<ItemDto> allItems = new ArrayList<>();
//
//            // Get all lost items
//            try {
//                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
//                log.info("Found {} lost items", lostItems.size());
//
//                // Convert lost items to ItemDto
//                for (LostItemResponse lostItem : lostItems) {
//                    try {
//                        ItemDto itemDto = convertLostItemToDto(lostItem);
//                        allItems.add(itemDto);
//                        log.debug("Added lost item: {} (ID: {}) by user: {}",
//                                itemDto.getTitle(), itemDto.getId(), itemDto.getContactInfo().getName());
//                    } catch (Exception e) {
//                        log.warn("Error converting lost item ID {}: {}", lostItem.getId(), e.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error fetching lost items: {}", e.getMessage());
//            }
//
//            // Get all found items
//            try {
//                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
//                log.info("Found {} found items", foundItems.size());
//
//                // Convert found items to ItemDto
//                for (FoundItemResponse foundItem : foundItems) {
//                    try {
//                        ItemDto itemDto = convertFoundItemToDto(foundItem);
//                        allItems.add(itemDto);
//                        log.debug("Added found item: {} (ID: {}) by user: {}",
//                                itemDto.getTitle(), itemDto.getId(), itemDto.getContactInfo().getName());
//                    } catch (Exception e) {
//                        log.warn("Error converting found item ID {}: {}", foundItem.getId(), e.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error fetching found items: {}", e.getMessage());
//            }
//
//            log.info("Returning {} total items", allItems.size());
//            return allItems;
//
//        } catch (Exception e) {
//            log.error("Error fetching all items: {}", e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    @Override
//    public ItemDto getItemById(String id) {
//        try {
//            Long itemId = Long.parseLong(id);
//            log.debug("Searching for item with ID: {}", itemId);
//
//            // Try to find in lost items first
//            try {
//                LostItemResponse lostItem = lostItemService.getLostItemById(itemId);
//                if (lostItem != null) {
//                    log.debug("Found item {} in lost items", itemId);
//                    return convertLostItemToDto(lostItem);
//                }
//            } catch (Exception e) {
//                log.debug("Item {} not found in lost items: {}", itemId, e.getMessage());
//            }
//
//            // Try to find in found items
//            try {
//                FoundItemResponse foundItem = foundItemService.getFoundItemById(itemId);
//                if (foundItem != null) {
//                    log.debug("Found item {} in found items", itemId);
//                    return convertFoundItemToDto(foundItem);
//                }
//            } catch (Exception e) {
//                log.debug("Item {} not found in found items: {}", itemId, e.getMessage());
//            }
//
//            log.warn("Item with ID {} not found anywhere", itemId);
//            return null;
//
//        } catch (NumberFormatException e) {
//            log.error("Invalid item ID format: {}", id);
//            return null;
//        } catch (Exception e) {
//            log.error("Error fetching item by ID {}: {}", id, e.getMessage(), e);
//            return null;
//        }
//    }
//
//    @Override
//    public List<ItemDto> getOppositeItems(String itemType) {
//        try {
//            log.info("Fetching opposite items for type: {}", itemType);
//
//            if ("LOST".equalsIgnoreCase(itemType)) {
//                // If requesting opposite of LOST, return FOUND items
//                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
//                log.debug("Converting {} found items", foundItems.size());
//
//                return foundItems.stream()
//                        .map(foundItem -> {
//                            try {
//                                return convertFoundItemToDto(foundItem);
//                            } catch (Exception e) {
//                                log.warn("Error converting found item {}: {}", foundItem.getId(), e.getMessage());
//                                return null;
//                            }
//                        })
//                        .filter(item -> item != null)
//                        .collect(Collectors.toList());
//
//            } else if ("FOUND".equalsIgnoreCase(itemType)) {
//                // If requesting opposite of FOUND, return LOST items
//                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
//                log.debug("Converting {} lost items", lostItems.size());
//
//                return lostItems.stream()
//                        .map(lostItem -> {
//                            try {
//                                return convertLostItemToDto(lostItem);
//                            } catch (Exception e) {
//                                log.warn("Error converting lost item {}: {}", lostItem.getId(), e.getMessage());
//                                return null;
//                            }
//                        })
//                        .filter(item -> item != null)
//                        .collect(Collectors.toList());
//            } else {
//                log.warn("Invalid item type requested: {}", itemType);
//                return new ArrayList<>();
//            }
//
//        } catch (Exception e) {
//            log.error("Error fetching opposite items for type {}: {}", itemType, e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    // FIXED: Robust user data fetching with multiple fallback strategies
//    private UserResponse fetchUserWithFallbacks(Long userId) {
//        if (userId == null) {
//            log.warn("User ID is null, using default user");
//            return createDefaultUser(1L);
//        }
//
//        try {
//            log.debug("Attempting to fetch user by ID: {}", userId);
//            UserResponse user = userServiceClient.getUserById(userId);
//
//            if (user != null) {
//                log.debug("Successfully fetched user: {} (ID: {})", user.getFullName(), user.getId());
//                return user;
//            } else {
//                log.warn("User service returned null for userId: {}", userId);
//                return createDefaultUser(userId);
//            }
//
//        } catch (FeignException e) {
//            log.error("Feign error fetching user {}: Status {}, Message: {}",
//                    userId, e.status(), e.getMessage());
//            return createDefaultUser(userId);
//
//        } catch (Exception e) {
//            log.error("Unexpected error fetching user {}: {}", userId, e.getMessage(), e);
//            return createDefaultUser(userId);
//        }
//    }
//
//    // Create a default user when real user data isn't available
//    private UserResponse createDefaultUser(Long userId) {
//        UserResponse defaultUser = new UserResponse();
//        defaultUser.setId(userId);
//        defaultUser.setFullName("Student " + userId); // More realistic than "Anonymous"
//        defaultUser.setEmail("student" + userId + "@university.edu");
//        // Add other fields as needed
//        return defaultUser;
//    }
//
//    // In ItemServiceImpl.java - Fix the convertLostItemToFrontend method
//    private ItemDto convertLostItemToDto(LostItemResponse lostItem) {
//        try {
//            // CRITICAL: Get REAL user data for each item
//            UserResponse user = userServiceClient.getUserById(lostItem.getReportedByUserId());
//
//            ItemDto dto = new ItemDto();
//            dto.setId("lost-" + lostItem.getId());
//            dto.setTitle(lostItem.getName());
//            dto.setDescription(lostItem.getDescription());
//            // ... other fields ...
//
//            // CRITICAL: Set real contact info
//            ContactInfo contactInfo = new ContactInfo();
//            if (user != null) {
//                contactInfo.setId(user.getId());
//                contactInfo.setName(user.getFullName() != null ? user.getFullName() : "Student " + user.getId());
//                contactInfo.setEmail(user.getEmail() != null ? user.getEmail() : "student" + user.getId() + "@university.edu");
//            } else {
//                // Fallback with real ID
//                contactInfo.setId(lostItem.getReportedByUserId());
//                contactInfo.setName("Student " + lostItem.getReportedByUserId());
//                contactInfo.setEmail("student" + lostItem.getReportedByUserId() + "@university.edu");
//            }
//
//            dto.setContactInfo(contactInfo);
//            dto.setUserEmail(contactInfo.getEmail());
//
//            return dto;
//        } catch (Exception e) {
//            log.error("Error converting lost item: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    // COMPLETELY REWRITTEN: Found item conversion with proper user handling
//    private ItemDto convertFoundItemToDto(FoundItemResponse foundItem) {
//        try {
//            log.debug("Converting found item: {} (ID: {}, User ID: {})",
//                    foundItem.getName(), foundItem.getId(), foundItem.getFoundByUserId());
//
//            ItemDto dto = new ItemDto();
//            dto.setId("found-" + foundItem.getId()); // Prefix to distinguish from lost items
//            dto.setTitle(foundItem.getName());
//            dto.setDescription(foundItem.getDescription());
//            dto.setCategory(foundItem.getCategoryName());
//            dto.setImageUrl(foundItem.getImageUrl());
//            dto.setUserId(foundItem.getFoundByUserId().toString());
//            dto.setItemType("FOUND");
//            dto.setLocation(foundItem.getLocation());
//            dto.setCreatedAt(foundItem.getReportedDate());
//
//            // CRITICAL: Fetch real user data and create proper contact info
//            UserResponse user = fetchUserWithFallbacks(foundItem.getFoundByUserId());
//
//            ContactInfo contactInfo = new ContactInfo();
//            contactInfo.setId(user.getId()); // REAL user ID, not default
//            contactInfo.setName(user.getFullName()); // REAL user name
//            contactInfo.setEmail(user.getEmail()); // REAL user email
//            contactInfo.setPhone(""); // Add phone if available in UserResponse
//
//            dto.setContactInfo(contactInfo);
//            dto.setUserEmail(user.getEmail()); // Set this too for consistency
//
//            log.info("✅ Found item DTO created: Item ID={}, User ID={}, User Name={}, Email={}",
//                    dto.getId(), contactInfo.getId(), contactInfo.getName(), contactInfo.getEmail());
//
//            return dto;
//
//        } catch (Exception e) {
//            log.error("❌ Error converting found item to DTO: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to convert found item: " + e.getMessage(), e);
//        }
//    }
//}

//
//// FIXED: ItemServiceImpl.java - Handle both prefixed and numeric IDs
//package edu.icet.ecom.service.custom.Impl;
//
//import edu.icet.ecom.client.UserServiceClient;
//import edu.icet.ecom.model.dto.ContactInfo;
//import edu.icet.ecom.model.dto.ItemDto;
//import edu.icet.ecom.model.dto.response.FoundItemResponse;
//import edu.icet.ecom.model.dto.response.LostItemResponse;
//import edu.icet.ecom.model.dto.response.UserResponse;
//import edu.icet.ecom.service.custom.FoundItemService;
//import edu.icet.ecom.service.custom.ItemService;
//import edu.icet.ecom.service.custom.LostItemService;
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ItemServiceImpl implements ItemService {
//
//    private final LostItemService lostItemService;
//    private final FoundItemService foundItemService;
//    private final UserServiceClient userServiceClient;
//
//    // ADDED: Helper method to extract numeric ID from any format
//    private Long extractNumericId(String itemId) {
//        if (itemId == null || itemId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Item ID cannot be null or empty");
//        }
//
//        // Remove prefixes like "lost-", "found-", etc.
//        String cleanId = itemId.replaceAll("^(lost-|found-)", "");
//
//        try {
//            return Long.parseLong(cleanId);
//        } catch (NumberFormatException e) {
//            throw new IllegalArgumentException("Invalid item ID format: " + itemId);
//        }
//    }
//
//    @Override
//    public List<ItemDto> getAllItems() {
//        try {
//            log.info("Fetching all items (lost and found)");
//            List<ItemDto> allItems = new ArrayList<>();
//
//            // Get all lost items
//            try {
//                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
//                log.info("Found {} lost items", lostItems.size());
//
//                // Convert lost items to ItemDto
//                for (LostItemResponse lostItem : lostItems) {
//                    try {
//                        ItemDto itemDto = convertLostItemToDto(lostItem);
//                        allItems.add(itemDto);
//                        log.debug("Added lost item: {} (ID: {}) by user: {}",
//                                itemDto.getTitle(), itemDto.getId(), itemDto.getContactInfo().getName());
//                    } catch (Exception e) {
//                        log.warn("Error converting lost item ID {}: {}", lostItem.getId(), e.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error fetching lost items: {}", e.getMessage());
//            }
//
//            // Get all found items
//            try {
//                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
//                log.info("Found {} found items", foundItems.size());
//
//                // Convert found items to ItemDto
//                for (FoundItemResponse foundItem : foundItems) {
//                    try {
//                        ItemDto itemDto = convertFoundItemToDto(foundItem);
//                        allItems.add(itemDto);
//                        log.debug("Added found item: {} (ID: {}) by user: {}",
//                                itemDto.getTitle(), itemDto.getId(), itemDto.getContactInfo().getName());
//                    } catch (Exception e) {
//                        log.warn("Error converting found item ID {}: {}", foundItem.getId(), e.getMessage());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error fetching found items: {}", e.getMessage());
//            }
//
//            log.info("Returning {} total items", allItems.size());
//            return allItems;
//
//        } catch (Exception e) {
//            log.error("Error fetching all items: {}", e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    // FIXED: Accept both formats - "123" or "lost-123"
//    @Override
//    public ItemDto getItemById(String id) {
//        try {
//            Long itemId = extractNumericId(id);
//            log.debug("Searching for item with numeric ID: {} (from input: {})", itemId, id);
//
//            // Try to find in lost items first
//            try {
//                LostItemResponse lostItem = lostItemService.getLostItemById(itemId);
//                if (lostItem != null) {
//                    log.debug("Found item {} in lost items", itemId);
//                    return convertLostItemToDto(lostItem);
//                }
//            } catch (Exception e) {
//                log.debug("Item {} not found in lost items: {}", itemId, e.getMessage());
//            }
//
//            // Try to find in found items
//            try {
//                FoundItemResponse foundItem = foundItemService.getFoundItemById(itemId);
//                if (foundItem != null) {
//                    log.debug("Found item {} in found items", itemId);
//                    return convertFoundItemToDto(foundItem);
//                }
//            } catch (Exception e) {
//                log.debug("Item {} not found in found items: {}", itemId, e.getMessage());
//            }
//
//            log.warn("Item with ID {} not found anywhere", itemId);
//            return null;
//
//        } catch (IllegalArgumentException e) {
//            log.error("Invalid item ID format: {}", id);
//            return null;
//        } catch (Exception e) {
//            log.error("Error fetching item by ID {}: {}", id, e.getMessage(), e);
//            return null;
//        }
//    }
//
//    @Override
//    public List<ItemDto> getOppositeItems(String itemType) {
//        try {
//            log.info("Fetching opposite items for type: {}", itemType);
//
//            if ("LOST".equalsIgnoreCase(itemType)) {
//                // If requesting opposite of LOST, return FOUND items
//                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
//                log.debug("Converting {} found items", foundItems.size());
//
//                return foundItems.stream()
//                        .map(foundItem -> {
//                            try {
//                                return convertFoundItemToDto(foundItem);
//                            } catch (Exception e) {
//                                log.warn("Error converting found item {}: {}", foundItem.getId(), e.getMessage());
//                                return null;
//                            }
//                        })
//                        .filter(item -> item != null)
//                        .collect(Collectors.toList());
//
//            } else if ("FOUND".equalsIgnoreCase(itemType)) {
//                // If requesting opposite of FOUND, return LOST items
//                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
//                log.debug("Converting {} lost items", lostItems.size());
//
//                return lostItems.stream()
//                        .map(lostItem -> {
//                            try {
//                                return convertLostItemToDto(lostItem);
//                            } catch (Exception e) {
//                                log.warn("Error converting lost item {}: {}", lostItem.getId(), e.getMessage());
//                                return null;
//                            }
//                        })
//                        .filter(item -> item != null)
//                        .collect(Collectors.toList());
//            } else {
//                log.warn("Invalid item type requested: {}", itemType);
//                return new ArrayList<>();
//            }
//
//        } catch (Exception e) {
//            log.error("Error fetching opposite items for type {}: {}", itemType, e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    // Robust user data fetching with multiple fallback strategies
//    private UserResponse fetchUserWithFallbacks(Long userId) {
//        if (userId == null) {
//            log.warn("User ID is null, using default user");
//            return createDefaultUser(1L);
//        }
//
//        try {
//            log.debug("Attempting to fetch user by ID: {}", userId);
//            UserResponse user = userServiceClient.getUserById(userId);
//
//            if (user != null) {
//                log.debug("Successfully fetched user: {} (ID: {})", user.getFullName(), user.getId());
//                return user;
//            } else {
//                log.warn("User service returned null for userId: {}", userId);
//                return createDefaultUser(userId);
//            }
//
//        } catch (FeignException e) {
//            log.error("Feign error fetching user {}: Status {}, Message: {}",
//                    userId, e.status(), e.getMessage());
//            return createDefaultUser(userId);
//
//        } catch (Exception e) {
//            log.error("Unexpected error fetching user {}: {}", userId, e.getMessage(), e);
//            return createDefaultUser(userId);
//        }
//    }
//
//    // Create a default user when real user data isn't available
//    private UserResponse createDefaultUser(Long userId) {
//        UserResponse defaultUser = new UserResponse();
//        defaultUser.setId(userId);
//        defaultUser.setFullName("Student " + userId);
//        defaultUser.setEmail("student" + userId + "@university.edu");
//        return defaultUser;
//    }
//
//    // FIXED: Lost item conversion with proper user data
//    private ItemDto convertLostItemToDto(LostItemResponse lostItem) {
//        try {
//            UserResponse user = fetchUserWithFallbacks(lostItem.getReportedByUserId());
//
//            ItemDto dto = new ItemDto();
//            // FIXED: Always include prefix for frontend consistency
//            dto.setId("lost-" + lostItem.getId());
//            dto.setTitle(lostItem.getName());
//            dto.setDescription(lostItem.getDescription());
//            dto.setCategory(lostItem.getCategoryName());
//            dto.setImageUrl(lostItem.getImageUrl());
//            dto.setUserId(lostItem.getReportedByUserId().toString());
//            dto.setItemType("LOST");
//            dto.setLocation(lostItem.getLocation());
//            dto.setCreatedAt(lostItem.getReportedDate());
//
//            // Set contact info with real user data
//            ContactInfo contactInfo = new ContactInfo();
//            contactInfo.setId(user.getId());
//            contactInfo.setName(user.getFullName() != null ? user.getFullName() : "Student " + user.getId());
//            contactInfo.setEmail(user.getEmail() != null ? user.getEmail() : "student" + user.getId() + "@university.edu");
//            contactInfo.setPhone("");
//
//            dto.setContactInfo(contactInfo);
//            dto.setUserEmail(contactInfo.getEmail());
//
//            return dto;
//        } catch (Exception e) {
//            log.error("Error converting lost item: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    // FIXED: Found item conversion with proper user data
//    private ItemDto convertFoundItemToDto(FoundItemResponse foundItem) {
//        try {
//            log.debug("Converting found item: {} (ID: {}, User ID: {})",
//                    foundItem.getName(), foundItem.getId(), foundItem.getFoundByUserId());
//
//            ItemDto dto = new ItemDto();
//            // FIXED: Always include prefix for frontend consistency
//            dto.setId("found-" + foundItem.getId());
//            dto.setTitle(foundItem.getName());
//            dto.setDescription(foundItem.getDescription());
//            dto.setCategory(foundItem.getCategoryName());
//            dto.setImageUrl(foundItem.getImageUrl());
//            dto.setUserId(foundItem.getFoundByUserId().toString());
//            dto.setItemType("FOUND");
//            dto.setLocation(foundItem.getLocation());
//            dto.setCreatedAt(foundItem.getReportedDate());
//
//            // Fetch real user data and create proper contact info
//            UserResponse user = fetchUserWithFallbacks(foundItem.getFoundByUserId());
//
//            ContactInfo contactInfo = new ContactInfo();
//            contactInfo.setId(user.getId());
//            contactInfo.setName(user.getFullName());
//            contactInfo.setEmail(user.getEmail());
//            contactInfo.setPhone("");
//
//            dto.setContactInfo(contactInfo);
//            dto.setUserEmail(user.getEmail());
//
//            log.info("Found item DTO created: Item ID={}, User ID={}, User Name={}, Email={}",
//                    dto.getId(), contactInfo.getId(), contactInfo.getName(), contactInfo.getEmail());
//
//            return dto;
//
//        } catch (Exception e) {
//            log.error("Error converting found item to DTO: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to convert found item: " + e.getMessage(), e);
//        }
//    }
//}


// CRITICAL FIX: ItemServiceImpl.java - Properly set itemType field
package edu.icet.ecom.service.custom.Impl;

import edu.icet.ecom.client.UserServiceClient;
import edu.icet.ecom.model.dto.ContactInfo;
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

    // Helper method to extract numeric ID from any format
    private Long extractNumericId(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }

        String cleanId = itemId.replaceAll("^(lost-|found-)", "");

        try {
            return Long.parseLong(cleanId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid item ID format: " + itemId);
        }
    }

    @Override
    public List<ItemDto> getAllItems() {
        try {
            log.info("Fetching all items (lost and found)");
            List<ItemDto> allItems = new ArrayList<>();

            try {
                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
                log.info("Found {} lost items", lostItems.size());

                for (LostItemResponse lostItem : lostItems) {
                    try {
                        ItemDto itemDto = convertLostItemToDto(lostItem);
                        allItems.add(itemDto);
                    } catch (Exception e) {
                        log.warn("Error converting lost item ID {}: {}", lostItem.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching lost items: {}", e.getMessage());
            }

            try {
                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
                log.info("Found {} found items", foundItems.size());

                for (FoundItemResponse foundItem : foundItems) {
                    try {
                        ItemDto itemDto = convertFoundItemToDto(foundItem);
                        allItems.add(itemDto);
                    } catch (Exception e) {
                        log.warn("Error converting found item ID {}: {}", foundItem.getId(), e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching found items: {}", e.getMessage());
            }

            log.info("Returning {} total items", allItems.size());
            return allItems;

        } catch (Exception e) {
            log.error("Error fetching all items: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ItemDto getItemById(String id) {
        try {
            Long itemId = extractNumericId(id);
            log.info("Searching for item with numeric ID: {} (from input: {})", itemId, id);

            // Try to find in lost items first
            try {
                LostItemResponse lostItem = lostItemService.getLostItemById(itemId);
                if (lostItem != null) {
                    log.info("Found item {} in lost items", itemId);
                    ItemDto result = convertLostItemToDto(lostItem);
                    log.info("Converted lost item - Type: {}, Title: {}", result.getItemType(), result.getTitle());
                    return result;
                }
            } catch (Exception e) {
                log.debug("Item {} not found in lost items: {}", itemId, e.getMessage());
            }

            // Try to find in found items
            try {
                FoundItemResponse foundItem = foundItemService.getFoundItemById(itemId);
                if (foundItem != null) {
                    log.info("Found item {} in found items", itemId);
                    ItemDto result = convertFoundItemToDto(foundItem);
                    log.info("Converted found item - Type: {}, Title: {}", result.getItemType(), result.getTitle());
                    return result;
                }
            } catch (Exception e) {
                log.debug("Item {} not found in found items: {}", itemId, e.getMessage());
            }

            log.warn("Item with ID {} not found anywhere", itemId);
            return null;

        } catch (IllegalArgumentException e) {
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
                List<FoundItemResponse> foundItems = foundItemService.getAllFoundItems(Pageable.unpaged());
                log.debug("Converting {} found items", foundItems.size());

                return foundItems.stream()
                        .map(foundItem -> {
                            try {
                                return convertFoundItemToDto(foundItem);
                            } catch (Exception e) {
                                log.warn("Error converting found item {}: {}", foundItem.getId(), e.getMessage());
                                return null;
                            }
                        })
                        .filter(item -> item != null)
                        .collect(Collectors.toList());

            } else if ("FOUND".equalsIgnoreCase(itemType)) {
                List<LostItemResponse> lostItems = lostItemService.getAllLostItems(Pageable.unpaged());
                log.debug("Converting {} lost items", lostItems.size());

                return lostItems.stream()
                        .map(lostItem -> {
                            try {
                                return convertLostItemToDto(lostItem);
                            } catch (Exception e) {
                                log.warn("Error converting lost item {}: {}", lostItem.getId(), e.getMessage());
                                return null;
                            }
                        })
                        .filter(item -> item != null)
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

    private UserResponse fetchUserWithFallbacks(Long userId) {
        if (userId == null) {
            log.warn("User ID is null, using default user");
            return createDefaultUser(1L);
        }

        try {
            log.debug("Attempting to fetch user by ID: {}", userId);
            UserResponse user = userServiceClient.getUserById(userId);

            if (user != null) {
                log.debug("Successfully fetched user: {} (ID: {})", user.getFullName(), user.getId());
                return user;
            } else {
                log.warn("User service returned null for userId: {}", userId);
                return createDefaultUser(userId);
            }

        } catch (FeignException e) {
            log.error("Feign error fetching user {}: Status {}, Message: {}",
                    userId, e.status(), e.getMessage());
            return createDefaultUser(userId);

        } catch (Exception e) {
            log.error("Unexpected error fetching user {}: {}", userId, e.getMessage(), e);
            return createDefaultUser(userId);
        }
    }

    private UserResponse createDefaultUser(Long userId) {
        UserResponse defaultUser = new UserResponse();
        defaultUser.setId(userId);
        defaultUser.setFullName("Student " + userId);
        defaultUser.setEmail("student" + userId + "@university.edu");
        return defaultUser;
    }

    // CRITICAL FIX: Ensure itemType is ALWAYS set correctly
    private ItemDto convertLostItemToDto(LostItemResponse lostItem) {
        try {
            UserResponse user = fetchUserWithFallbacks(lostItem.getReportedByUserId());

            ItemDto dto = new ItemDto();
            dto.setId("lost-" + lostItem.getId());
            dto.setTitle(lostItem.getName());
            dto.setDescription(lostItem.getDescription());
            dto.setCategory(lostItem.getCategoryName());
            dto.setImageUrl(lostItem.getImageUrl());
            dto.setUserId(lostItem.getReportedByUserId().toString());

            // CRITICAL FIX: ALWAYS set itemType
            dto.setItemType("LOST");

            dto.setLocation(lostItem.getLocation());
            dto.setCreatedAt(lostItem.getReportedDate());

            // Set contact info
            ContactInfo contactInfo = new ContactInfo();
            contactInfo.setId(user.getId());
            contactInfo.setName(user.getFullName() != null ? user.getFullName() : "Student " + user.getId());
            contactInfo.setEmail(user.getEmail() != null ? user.getEmail() : "student" + user.getId() + "@university.edu");
            contactInfo.setPhone("");

            dto.setContactInfo(contactInfo);
            dto.setUserEmail(contactInfo.getEmail());

            // VALIDATION: Ensure itemType is set
            if (dto.getItemType() == null) {
                log.error("CRITICAL: ItemType is null for lost item {}", lostItem.getId());
                dto.setItemType("LOST"); // Force set it
            }

            log.info("Lost item conversion completed: ID={}, Type={}, Title={}",
                    dto.getId(), dto.getItemType(), dto.getTitle());

            return dto;
        } catch (Exception e) {
            log.error("Error converting lost item: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ItemDto convertFoundItemToDto(FoundItemResponse foundItem) {
        try {
            ItemDto dto = new ItemDto();
            dto.setId("found-" + foundItem.getId());
            dto.setTitle(foundItem.getName());
            dto.setDescription(foundItem.getDescription());
            dto.setCategory(foundItem.getCategoryName());
            dto.setImageUrl(foundItem.getImageUrl());
            dto.setUserId(foundItem.getFoundByUserId().toString());

            // CRITICAL FIX: ALWAYS set itemType
            dto.setItemType("FOUND");

            dto.setLocation(foundItem.getLocation());
            dto.setCreatedAt(foundItem.getReportedDate());

            UserResponse user = fetchUserWithFallbacks(foundItem.getFoundByUserId());

            ContactInfo contactInfo = new ContactInfo();
            contactInfo.setId(user.getId());
            contactInfo.setName(user.getFullName());
            contactInfo.setEmail(user.getEmail());
            contactInfo.setPhone("");

            dto.setContactInfo(contactInfo);
            dto.setUserEmail(user.getEmail());

            // VALIDATION: Ensure itemType is set
            if (dto.getItemType() == null) {
                log.error("CRITICAL: ItemType is null for found item {}", foundItem.getId());
                dto.setItemType("FOUND"); // Force set it
            }

            log.info("Found item conversion completed: ID={}, Type={}, Title={}",
                    dto.getId(), dto.getItemType(), dto.getTitle());

            return dto;

        } catch (Exception e) {
            log.error("Error converting found item to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert found item: " + e.getMessage(), e);
        }
    }
}