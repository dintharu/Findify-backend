package edu.icet.ecom.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Data
public class CreateFoundItemRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Description is required")
    private String description;
    @NotBlank(message = "Location is required")
    private String location;
    @NotNull
    private Long categoryId;
    @NotNull
    private LocalDateTime foundDate;
    private String imageUrl;
}
