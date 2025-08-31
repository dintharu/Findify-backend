package edu.icet.ecom.model.dto.response;

import edu.icet.ecom.enums.ItemStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostItemResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String categoryName;
    private Long reportedByUserId;
    private LocalDateTime lostDate;
    private LocalDateTime reportedDate;
    private ItemStatus status;
    private String imageUrl;
}
