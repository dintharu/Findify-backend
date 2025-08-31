package edu.icet.ecom.model.dto;

import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageSimilarityItemDto {

    private String id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String userId;
    private String userEmail;
    private String itemType; // "LOST" or "FOUND"
    private String location;
    private LocalDateTime createdAt;

    // Static factory method for LostItemResponse
    public static ImageSimilarityItemDto fromLostItem(LostItemResponse lostItem, String userEmail) {
        ImageSimilarityItemDto dto = new ImageSimilarityItemDto();
        dto.setId(lostItem.getId().toString());
        dto.setTitle(lostItem.getName());
        dto.setDescription(lostItem.getDescription());
        dto.setCategory(lostItem.getCategoryName()); // Assuming you have category name
        dto.setImageUrl(lostItem.getImageUrl());
        dto.setUserId(lostItem.getReportedByUserId().toString());
        dto.setUserEmail(userEmail);
        dto.setItemType("LOST");
        dto.setLocation(lostItem.getLocation());
        dto.setCreatedAt(lostItem.getReportedDate());
        return dto;
    }

    // Static factory method for FoundItemResponse
    public static ImageSimilarityItemDto fromFoundItem(FoundItemResponse foundItem, String userEmail) {
        ImageSimilarityItemDto dto = new ImageSimilarityItemDto();
        dto.setId(foundItem.getId().toString());
        dto.setTitle(foundItem.getName());
        dto.setDescription(foundItem.getDescription());
        dto.setCategory(foundItem.getCategoryName()); // Assuming you have category name
        dto.setImageUrl(foundItem.getImageUrl());
        dto.setUserId(foundItem.getFoundByUserId().toString());
        dto.setUserEmail(userEmail);
        dto.setItemType("FOUND");
        dto.setLocation(foundItem.getLocation());
        dto.setCreatedAt(foundItem.getReportedDate());
        return dto;
    }
}
