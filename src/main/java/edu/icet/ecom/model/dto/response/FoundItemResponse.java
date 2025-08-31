package edu.icet.ecom.model.dto.response;

import edu.icet.ecom.enums.ItemStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoundItemResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String categoryName;
    private Long foundByUserId;
    private LocalDateTime foundDate;
    private LocalDateTime reportedDate;
    private ItemStatus status;
    private String imageUrl;
}
