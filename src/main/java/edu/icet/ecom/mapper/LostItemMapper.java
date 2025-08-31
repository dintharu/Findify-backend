package edu.icet.ecom.mapper;

import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import edu.icet.ecom.model.entity.LostItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LostItemMapper {

    @Autowired
    ModelMapper modelMapper;

    public LostItem toEntity(CreateLostItemRequest request) {
        if (request == null) return null;

        LostItem lostItem = new LostItem();
        lostItem.setName(request.getName());
        lostItem.setDescription(request.getDescription());
        lostItem.setLocation(request.getLocation());
        lostItem.setLostDate(request.getLostDate());
        lostItem.setReportedDate(LocalDateTime.now());

        // Set image URL if provided
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            lostItem.setImageUrl(request.getImageUrl());
        }

        return lostItem;
    }

    public LostItemResponse toResponse(LostItem lostItem) {
        if (lostItem == null) return null;

        // Use ModelMapper for basic mapping
        LostItemResponse response = modelMapper.map(lostItem, LostItemResponse.class);

        // Manually set the category name from the Category entity
        if (lostItem.getCategory() != null) {
            response.setCategoryName(lostItem.getCategory().getName());
        }

        return response;
    }
}