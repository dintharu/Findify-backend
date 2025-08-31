// ItemDto.java
package edu.icet.ecom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String id;
    private String title;
    private String description;
    private String category;
    private String location;
    private String imageUrl;
    private String itemType; // "LOST" or "FOUND"
    private String userId;
    private String userEmail;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
}
