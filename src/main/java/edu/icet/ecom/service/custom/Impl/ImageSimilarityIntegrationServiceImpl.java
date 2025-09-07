//package edu.icet.ecom.service.custom.Impl;
//
//import edu.icet.ecom.client.ImageSimilarityClient;
//import edu.icet.ecom.client.UserServiceClient;
//import edu.icet.ecom.model.dto.ImageSimilarityItemDto;
//import edu.icet.ecom.model.dto.response.FoundItemResponse;
//import edu.icet.ecom.model.dto.response.LostItemResponse;
//import edu.icet.ecom.model.dto.response.UserResponse;
//import edu.icet.ecom.service.custom.ImageSimilarityIntegrationService;
//import feign.FeignException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ImageSimilarityIntegrationServiceImpl implements ImageSimilarityIntegrationService {
//
//    private final ImageSimilarityClient imageSimilarityClient;
//    private final UserServiceClient userServiceClient;
//
//    @Override
//    @Async // Make this asynchronous so it doesn't block the main item creation
//    public void checkForMatchesAfterLostItemCreated(LostItemResponse lostItem) {
//        try {
//            log.info("Triggering image similarity check for lost item: {}", lostItem.getName());
//
//            // Validate required fields
//            if (lostItem.getImageUrl() == null || lostItem.getImageUrl().trim().isEmpty()) {
//                log.warn("Skipping image similarity check for lost item {} - no image URL", lostItem.getId());
//                return;
//            }
//
//            // Get user email from user service
//            String userEmail = getUserEmail(lostItem.getReportedByUserId());
//            if (userEmail == null) {
//                log.warn("Skipping image similarity check for lost item {} - couldn't get user email", lostItem.getId());
//                return;
//            }
//
//            // Convert to unified DTO
//            ImageSimilarityItemDto itemDto = ImageSimilarityItemDto.fromLostItem(lostItem, userEmail);
//
//            // Call image similarity service
//            try {
//                imageSimilarityClient.findMatches(itemDto);
//                log.info("Successfully triggered image similarity check for lost item ID: {} (User: {})",
//                        lostItem.getId(), userEmail);
//            } catch (FeignException e) {
//                log.error("Feign error calling image similarity service for lost item ID: {}. Status: {}, Message: {}",
//                        lostItem.getId(), e.status(), e.getMessage());
//            }
//
//        } catch (Exception e) {
//            log.error("Error triggering image similarity check for lost item ID: {}. Error: {}",
//                    lostItem.getId(), e.getMessage(), e);
//            // Don't throw exception - we don't want item creation to fail if similarity check fails
//        }
//    }
//
//    @Override
//    @Async // Make this asynchronous so it doesn't block the main item creation
//    public void checkForMatchesAfterFoundItemCreated(FoundItemResponse foundItem) {
//        try {
//            log.info("Triggering image similarity check for found item: {}", foundItem.getName());
//
//            // Validate required fields
//            if (foundItem.getImageUrl() == null || foundItem.getImageUrl().trim().isEmpty()) {
//                log.warn("Skipping image similarity check for found item {} - no image URL", foundItem.getId());
//                return;
//            }
//
//            // Get user email from user service
//            String userEmail = getUserEmail(foundItem.getFoundByUserId());
//            if (userEmail == null) {
//                log.warn("Skipping image similarity check for found item {} - couldn't get user email", foundItem.getId());
//                return;
//            }
//
//            // Convert to unified DTO
//            ImageSimilarityItemDto itemDto = ImageSimilarityItemDto.fromFoundItem(foundItem, userEmail);
//
//            // Call image similarity service
//            try {
//                imageSimilarityClient.findMatches(itemDto);
//                log.info("Successfully triggered image similarity check for found item ID: {} (User: {})",
//                        foundItem.getId(), userEmail);
//            } catch (FeignException e) {
//                log.error("Feign error calling image similarity service for found item ID: {}. Status: {}, Message: {}",
//                        foundItem.getId(), e.status(), e.getMessage());
//            }
//
//        } catch (Exception e) {
//            log.error("Error triggering image similarity check for found item ID: {}. Error: {}",
//                    foundItem.getId(), e.getMessage(), e);
//            // Don't throw exception - we don't want item creation to fail if similarity check fails
//        }
//    }
//
//    @Override
//    public String getUserEmail(Long userId) {
//        try {
//            log.debug("Fetching user email for userId: {}", userId);
//
//            UserResponse user = userServiceClient.getUserById(userId);
//            if (user != null && user.getEmail() != null) {
//                log.debug("Successfully retrieved email for userId {}: {}", userId, user.getEmail());
//                return user.getEmail();
//            } else {
//                log.warn("User found but email is null for userId: {}", userId);
//                return null;
//            }
//
//        } catch (FeignException e) {
//            log.error("Feign error getting user email for userId {}: Status {}, Message: {}",
//                    userId, e.status(), e.getMessage());
//            return null;
//        } catch (Exception e) {
//            log.error("Error getting user email for userId {}: {}", userId, e.getMessage(), e);
//            return null;
//        }
//    }
//}

package edu.icet.ecom.service.custom.Impl;

import edu.icet.ecom.client.ImageSimilarityClient;
import edu.icet.ecom.client.UserServiceClient;
import edu.icet.ecom.model.dto.ImageSimilarityItemDto;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import edu.icet.ecom.model.dto.response.UserResponse;
import edu.icet.ecom.service.custom.ImageSimilarityIntegrationService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageSimilarityIntegrationServiceImpl implements ImageSimilarityIntegrationService {

    private final ImageSimilarityClient imageSimilarityClient;
    private final UserServiceClient userServiceClient;

    @Override
    @Async
    public void checkForMatchesAfterLostItemCreated(LostItemResponse lostItem) {
        try {
            log.info("🔍 Starting image similarity check for lost item: {} (ID: {})",
                    lostItem.getName(), lostItem.getId());

            // CRITICAL: Get real user data for the person who posted the lost item
            UserResponse user = fetchUserDataSafely(lostItem.getReportedByUserId());

            if (user == null) {
                log.warn("⚠️ Cannot proceed with image matching - user data not available for lost item {}",
                        lostItem.getId());
                return;
            }

            // Create ImageSimilarityItemDto with REAL user data
            ImageSimilarityItemDto itemDto = createImageSimilarityDto(lostItem, user, "LOST");

            log.info("📤 Sending lost item to image similarity service: User={} ({}), Item={}",
                    user.getFullName(), user.getEmail(), lostItem.getName());

            // Send to image similarity service
            imageSimilarityClient.findMatches(itemDto);

            log.info("✅ Lost item sent to image similarity service successfully");

        } catch (Exception e) {
            log.error("❌ Error during lost item image similarity check: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void checkForMatchesAfterFoundItemCreated(FoundItemResponse foundItem) {
        try {
            log.info("🔍 Starting image similarity check for found item: {} (ID: {})",
                    foundItem.getName(), foundItem.getId());

            // CRITICAL: Get real user data for the person who posted the found item
            UserResponse user = fetchUserDataSafely(foundItem.getFoundByUserId());

            if (user == null) {
                log.warn("⚠️ Cannot proceed with image matching - user data not available for found item {}",
                        foundItem.getId());
                return;
            }

            // Create ImageSimilarityItemDto with REAL user data
            ImageSimilarityItemDto itemDto = createImageSimilarityDto(foundItem, user, "FOUND");

            log.info("📤 Sending found item to image similarity service: User={} ({}), Item={}",
                    user.getFullName(), user.getEmail(), foundItem.getName());

            // Send to image similarity service
            imageSimilarityClient.findMatches(itemDto);

            log.info("✅ Found item sent to image similarity service successfully");

        } catch (Exception e) {
            log.error("❌ Error during found item image similarity check: {}", e.getMessage(), e);
        }
    }

    // CRITICAL: Safe user data fetching with comprehensive error handling
    private UserResponse fetchUserDataSafely(Long userId) {
        if (userId == null) {
            log.error("❌ User ID is null - cannot fetch user data");
            return null;
        }

        try {
            log.debug("🔍 Fetching user data for ID: {}", userId);

            UserResponse user = userServiceClient.getUserById(userId);

            if (user == null) {
                log.warn("⚠️ User service returned null for user ID: {}", userId);
                return null;
            }

            // Validate that we have the essential data
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.warn("⚠️ User {} has no email - this will cause chat issues", userId);
                // Still return the user, but with a generated email
                user.setEmail("student" + userId + "@university.edu");
            }

            if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
                log.warn("⚠️ User {} has no name - setting default", userId);
                user.setFullName("Student " + userId);
            }

            log.info("✅ User data fetched successfully: {} (ID: {}, Email: {})",
                    user.getFullName(), user.getId(), user.getEmail());

            return user;

        } catch (FeignException e) {
            log.error("❌ Feign error fetching user {}: Status {}, Message: {}",
                    userId, e.status(), e.getMessage());
            return null;

        } catch (Exception e) {
            log.error("❌ Unexpected error fetching user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    // OVERLOADED: Create DTO from LostItemResponse
    private ImageSimilarityItemDto createImageSimilarityDto(LostItemResponse lostItem, UserResponse user, String itemType) {
        ImageSimilarityItemDto dto = new ImageSimilarityItemDto();

        // Item information
        dto.setId("lost-" + lostItem.getId()); // Prefix to distinguish lost/found
        dto.setTitle(lostItem.getName());
        dto.setDescription(lostItem.getDescription());
        dto.setCategory(lostItem.getCategoryName());
        dto.setImageUrl(lostItem.getImageUrl());
        dto.setLocation(lostItem.getLocation());
        dto.setItemType(itemType); // "LOST"
        dto.setCreatedAt(lostItem.getReportedDate());

        // CRITICAL: Real user information
        dto.setUserId(user.getId().toString());
        dto.setUserEmail(user.getEmail()); // This is what the chat service uses for identification!

        log.debug("📦 Created ImageSimilarityItemDto for lost item: ID={}, User={} ({})",
                dto.getId(), user.getFullName(), user.getEmail());

        return dto;
    }

    // OVERLOADED: Create DTO from FoundItemResponse
    private ImageSimilarityItemDto createImageSimilarityDto(FoundItemResponse foundItem, UserResponse user, String itemType) {
        ImageSimilarityItemDto dto = new ImageSimilarityItemDto();

        // Item information
        dto.setId("found-" + foundItem.getId()); // Prefix to distinguish lost/found
        dto.setTitle(foundItem.getName());
        dto.setDescription(foundItem.getDescription());
        dto.setCategory(foundItem.getCategoryName());
        dto.setImageUrl(foundItem.getImageUrl());
        dto.setLocation(foundItem.getLocation());
        dto.setItemType(itemType); // "FOUND"
        dto.setCreatedAt(foundItem.getReportedDate());

        // CRITICAL: Real user information
        dto.setUserId(user.getId().toString());
        dto.setUserEmail(user.getEmail()); // This is what the chat service uses for identification!

        log.debug("📦 Created ImageSimilarityItemDto for found item: ID={}, User={} ({})",
                dto.getId(), user.getFullName(), user.getEmail());

        return dto;
    }
}