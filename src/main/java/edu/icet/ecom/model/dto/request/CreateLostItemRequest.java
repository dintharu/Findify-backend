package edu.icet.ecom.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateLostItemRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Lost date is required")
    private LocalDateTime lostDate;

    private String imageUrl;
}