package edu.icet.ecom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private String id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String userId;
    private String userEmail;
    private String itemType; // LOST or FOUND
    private String location;
    private LocalDateTime createdAt;

}
