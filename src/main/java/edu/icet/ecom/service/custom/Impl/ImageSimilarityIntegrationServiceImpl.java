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
@Slf4j
@RequiredArgsConstructor
public class ImageSimilarityIntegrationServiceImpl implements ImageSimilarityIntegrationService {

    private final ImageSimilarityClient imageSimilarityClient;
    private final UserServiceClient userServiceClient;

    @Override
    @Async // Make this asynchronous so it doesn't block the main item creation
    public void checkForMatchesAfterLostItemCreated(LostItemResponse lostItem) {
        try {
            log.info("Triggering image similarity check for lost item: {}", lostItem.getName());

            // Validate required fields
            if (lostItem.getImageUrl() == null || lostItem.getImageUrl().trim().isEmpty()) {
                log.warn("Skipping image similarity check for lost item {} - no image URL", lostItem.getId());
                return;
            }

            // Get user email from user service
            String userEmail = getUserEmail(lostItem.getReportedByUserId());
            if (userEmail == null) {
                log.warn("Skipping image similarity check for lost item {} - couldn't get user email", lostItem.getId());
                return;
            }

            // Convert to unified DTO
            ImageSimilarityItemDto itemDto = ImageSimilarityItemDto.fromLostItem(lostItem, userEmail);

            // Call image similarity service
            try {
                imageSimilarityClient.findMatches(itemDto);
                log.info("Successfully triggered image similarity check for lost item ID: {} (User: {})",
                        lostItem.getId(), userEmail);
            } catch (FeignException e) {
                log.error("Feign error calling image similarity service for lost item ID: {}. Status: {}, Message: {}",
                        lostItem.getId(), e.status(), e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error triggering image similarity check for lost item ID: {}. Error: {}",
                    lostItem.getId(), e.getMessage(), e);
            // Don't throw exception - we don't want item creation to fail if similarity check fails
        }
    }

    @Override
    @Async // Make this asynchronous so it doesn't block the main item creation
    public void checkForMatchesAfterFoundItemCreated(FoundItemResponse foundItem) {
        try {
            log.info("Triggering image similarity check for found item: {}", foundItem.getName());

            // Validate required fields
            if (foundItem.getImageUrl() == null || foundItem.getImageUrl().trim().isEmpty()) {
                log.warn("Skipping image similarity check for found item {} - no image URL", foundItem.getId());
                return;
            }

            // Get user email from user service
            String userEmail = getUserEmail(foundItem.getFoundByUserId());
            if (userEmail == null) {
                log.warn("Skipping image similarity check for found item {} - couldn't get user email", foundItem.getId());
                return;
            }

            // Convert to unified DTO
            ImageSimilarityItemDto itemDto = ImageSimilarityItemDto.fromFoundItem(foundItem, userEmail);

            // Call image similarity service
            try {
                imageSimilarityClient.findMatches(itemDto);
                log.info("Successfully triggered image similarity check for found item ID: {} (User: {})",
                        foundItem.getId(), userEmail);
            } catch (FeignException e) {
                log.error("Feign error calling image similarity service for found item ID: {}. Status: {}, Message: {}",
                        foundItem.getId(), e.status(), e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error triggering image similarity check for found item ID: {}. Error: {}",
                    foundItem.getId(), e.getMessage(), e);
            // Don't throw exception - we don't want item creation to fail if similarity check fails
        }
    }

    @Override
    public String getUserEmail(Long userId) {
        try {
            log.debug("Fetching user email for userId: {}", userId);

            UserResponse user = userServiceClient.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                log.debug("Successfully retrieved email for userId {}: {}", userId, user.getEmail());
                return user.getEmail();
            } else {
                log.warn("User found but email is null for userId: {}", userId);
                return null;
            }

        } catch (FeignException e) {
            log.error("Feign error getting user email for userId {}: Status {}, Message: {}",
                    userId, e.status(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error getting user email for userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}