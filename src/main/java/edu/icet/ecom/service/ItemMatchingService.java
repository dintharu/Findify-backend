package edu.icet.ecom.service;

import edu.icet.ecom.client.EmailServiceClient;
import edu.icet.ecom.client.ItemServiceClient;
import edu.icet.ecom.model.dto.ItemDto;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemMatchingService {

    @Autowired
    private ItemServiceClient itemServiceClient;

    @Autowired
    private EmailServiceClient emailServiceClient;

    @Autowired
    private ImageSimilarityService imageSimilarityService;

    // Add this flag to enable/disable same user filtering for testing
    private static final boolean ENABLE_SAME_USER_FILTERING = false; // Set to false for testing

    public void findMatches(ItemDto newItem) {
        try {
            System.out.println("=== STARTING MATCH SEARCH ===");
            System.out.println("New item details:");
            System.out.println("  Title: " + newItem.getTitle());
            System.out.println("  Type: " + newItem.getItemType());
            System.out.println("  User: " + newItem.getUserEmail());
            System.out.println("  Image URL: " + (newItem.getImageUrl() != null ? "Present (length: " + newItem.getImageUrl().length() + ")" : "NULL"));
            System.out.println("  Category: " + newItem.getCategory());
            System.out.println("Same user filtering: " + (ENABLE_SAME_USER_FILTERING ? "ENABLED" : "DISABLED"));

            // Validate new item has required fields
            if (newItem.getImageUrl() == null || newItem.getImageUrl().trim().isEmpty()) {
                System.err.println("ERROR: New item has no image URL - cannot perform similarity matching");
                return;
            }

            // Check email service availability (but don't stop if it's down)
            boolean emailServiceAvailable = checkEmailServiceAvailability();

            if (!emailServiceAvailable) {
                System.err.println("WARNING: Email service is not available. Matches will be found but notifications won't be sent.");
            }

            // Get ALL items first, then filter for opposite type locally
            List<ItemDto> allItems;
            try {
                allItems = itemServiceClient.getAllItems();
                System.out.println("Retrieved " + allItems.size() + " total items from item service");

                // Debug: Show what we got
                if (allItems.isEmpty()) {
                    System.err.println("ERROR: No items returned from item service!");
                    return;
                } else {
                    System.out.println("Items breakdown:");
                    long lostCount = allItems.stream().filter(item -> "LOST".equals(item.getItemType())).count();
                    long foundCount = allItems.stream().filter(item -> "FOUND".equals(item.getItemType())).count();
                    System.out.println("  LOST items: " + lostCount);
                    System.out.println("  FOUND items: " + foundCount);
                }

            } catch (FeignException e) {
                System.err.println("Feign error retrieving items from item service: " + e.getMessage());
                System.err.println("Status: " + e.status());
                System.err.println("Content: " + e.contentUTF8());
                return;
            } catch (Exception e) {
                System.err.println("General error retrieving items from item service: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // Filter for opposite items locally (if new item is LOST, get FOUND items and vice versa)
            String oppositeType = newItem.getItemType().equals("LOST") ? "FOUND" : "LOST";
            List<ItemDto> oppositeItems = allItems.stream()
                    .filter(item -> {
                        if (item.getItemType() == null) {
                            System.out.println("Skipping item with null type: " + item.getTitle());
                            return false;
                        }
                        return oppositeType.equals(item.getItemType());
                    })
                    .collect(Collectors.toList());

            System.out.println("Checking " + oppositeItems.size() + " " + oppositeType + " items for matches...");

            if (oppositeItems.isEmpty()) {
                System.out.println("No " + oppositeType + " items found to match against");
                return;
            }

            // Debug: Show opposite items
            System.out.println("Opposite items to check:");
            oppositeItems.forEach(item -> {
                System.out.println("  - " + item.getTitle() + " (ID: " + item.getId() +
                        ", User: " + item.getUserEmail() + ", Image: " +
                        (item.getImageUrl() != null && !item.getImageUrl().isEmpty() ? "Present (length: " + item.getImageUrl().length() + ")" : "NULL") + ")");
            });

            int matchesFound = 0;
            int itemsProcessed = 0;
            int itemsSkipped = 0;

            for (ItemDto existingItem : oppositeItems) {
                try {
                    itemsProcessed++;
                    System.out.println("\n--- Processing item " + itemsProcessed + "/" + oppositeItems.size() + " ---");

                    // Skip if same user (only if filtering is enabled)
                    if (ENABLE_SAME_USER_FILTERING &&
                            existingItem.getUserId() != null && existingItem.getUserId().equals(newItem.getUserId())) {
                        System.out.println("Skipping same user item: " + existingItem.getTitle());
                        itemsSkipped++;
                        continue;
                    }

                    // Check category match first (optimization) - but be more flexible
                    if (existingItem.getCategory() != null && newItem.getCategory() != null &&
                            !existingItem.getCategory().equalsIgnoreCase(newItem.getCategory())) {
                        System.out.println("Category mismatch - New: '" + newItem.getCategory() +
                                "', Existing: '" + existingItem.getCategory() + "' - Continuing anyway...");
                        // Don't skip for category mismatch, just log it
                    }

                    System.out.println("Calculating similarity between:");
                    System.out.println("  New item: " + newItem.getTitle() + " (" + newItem.getUserEmail() + ")");
                    System.out.println("  Existing: " + existingItem.getTitle() + " (" + existingItem.getUserEmail() + ")");

                    // Validate image URLs before processing
                    if (existingItem.getImageUrl() == null || existingItem.getImageUrl().trim().isEmpty()) {
                        System.out.println("Skipping similarity check - existing item missing image URL");
                        itemsSkipped++;
                        continue;
                    }

                    System.out.println("Both images are available for comparison");

                    // Calculate image similarity
                    double similarity = imageSimilarityService.calculateSimilarity(
                            newItem.getImageUrl(),
                            existingItem.getImageUrl()
                    );

                    System.out.println("Final similarity score: " + String.format("%.2f", similarity * 100) + "%");

                    if (imageSimilarityService.isSimilarEnough(similarity)) {
                        System.out.println("🎉 MATCH FOUND! 🎉");

                        // Log match details even if email fails
                        logMatchDetails(newItem, existingItem, similarity);

                        // Try to send notifications if email service is available
                        if (emailServiceAvailable) {
                            sendMatchNotifications(newItem, existingItem, similarity);
                        } else {
                            System.err.println("Email service unavailable - Match found but notifications not sent");
                        }

                        matchesFound++;
                    } else {
                        System.out.println("Similarity too low for match (threshold: " +
                                String.format("%.1f", imageSimilarityService.isSimilarEnough(0.75) ? 75.0 : 65.0) + "%)");
                    }
                } catch (Exception e) {
                    System.err.println("Error processing item " + existingItem.getTitle() + ": " + e.getMessage());
                    e.printStackTrace();
                    itemsSkipped++;
                }
            }

            System.out.println("\n=== MATCH SEARCH COMPLETED ===");
            System.out.println("Items processed: " + itemsProcessed);
            System.out.println("Items skipped: " + itemsSkipped);
            System.out.println("Matches found: " + matchesFound);
            System.out.println("===============================");

        } catch (Exception e) {
            System.err.println("Fatal error in finding matches: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkEmailServiceAvailability() {
        try {
            emailServiceClient.healthCheck();
            System.out.println("Email service is available");
            return true;
        } catch (Exception e) {
            System.err.println("Email service is not available: " + e.getMessage());
            return false;
        }
    }

    private void logMatchDetails(ItemDto item1, ItemDto item2, double similarity) {
        ItemDto lostItem = item1.getItemType().equals("LOST") ? item1 : item2;
        ItemDto foundItem = item1.getItemType().equals("FOUND") ? item1 : item2;
        int similarityPercent = (int) Math.round(similarity * 100);

        System.out.println("\n🎯 === MATCH FOUND === 🎯");
        System.out.println("Lost item: " + lostItem.getTitle() + " (Owner: " + lostItem.getUserEmail() + ")");
        System.out.println("Found item: " + foundItem.getTitle() + " (Finder: " + foundItem.getUserEmail() + ")");
        System.out.println("Similarity: " + similarityPercent + "%");
        System.out.println("Lost location: " + (lostItem.getLocation() != null ? lostItem.getLocation() : "Not specified"));
        System.out.println("Found location: " + (foundItem.getLocation() != null ? foundItem.getLocation() : "Not specified"));
        System.out.println("========================== 🎯");
    }

    private void sendMatchNotifications(ItemDto item1, ItemDto item2, double similarity) {
        try {
            // Determine which is lost and which is found
            ItemDto lostItem = item1.getItemType().equals("LOST") ? item1 : item2;
            ItemDto foundItem = item1.getItemType().equals("FOUND") ? item1 : item2;

            int similarityPercent = (int) Math.round(similarity * 100);

            // Create detailed match messages
            String lostItemMessage = String.format(
                    "Great news! We found a potential match for your lost item '%s'. " +
                            "Someone found a '%s' that matches %d%% with your item. " +
                            "Location where it was found: %s. " +
                            "Please check your dashboard to contact the finder and arrange pickup.",
                    lostItem.getTitle(), foundItem.getTitle(), similarityPercent,
                    foundItem.getLocation() != null ? foundItem.getLocation() : "Not specified"
            );

            String foundItemMessage = String.format(
                    "Your found item '%s' matches someone's lost item! " +
                            "The lost item '%s' matches %d%% with what you found. " +
                            "The owner lost it at: %s. " +
                            "Please check your dashboard to contact them and arrange return.",
                    foundItem.getTitle(), lostItem.getTitle(), similarityPercent,
                    lostItem.getLocation() != null ? lostItem.getLocation() : "Not specified"
            );

            System.out.println("Sending notification to lost item owner: " + lostItem.getUserEmail());

            // Send to person who lost the item
            try {
                emailServiceClient.sendItemMatchedNotification(
                        Long.parseLong(lostItem.getUserId()),
                        lostItem.getUserEmail(),
                        lostItem.getTitle(),
                        lostItemMessage
                );
                System.out.println("✅ Notification sent successfully to lost item owner");
            } catch (Exception e) {
                System.err.println("❌ Failed to send notification to lost item owner: " + e.getMessage());
            }

            System.out.println("Sending notification to found item finder: " + foundItem.getUserEmail());

            // Send to person who found the item
            try {
                emailServiceClient.sendItemMatchedNotification(
                        Long.parseLong(foundItem.getUserId()),
                        foundItem.getUserEmail(),
                        foundItem.getTitle(),
                        foundItemMessage
                );
                System.out.println("✅ Notification sent successfully to found item finder");
            } catch (Exception e) {
                System.err.println("❌ Failed to send notification to found item finder: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Error sending match notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
}