package edu.icet.ecom.mapper;

import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.entity.FoundItems;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FoundItemMapper {

    @Autowired
    ModelMapper modelMapper;

    public FoundItems toEntity(CreateFoundItemRequest request) {
        if (request == null) return null;

        FoundItems foundItems = new FoundItems();
        foundItems.setName(request.getName());
        foundItems.setDescription(request.getDescription());
        foundItems.setLocation(request.getLocation());
        foundItems.setFoundDate(request.getFoundDate());
        foundItems.setImageUrl(request.getImageUrl());
        return foundItems;
    }

    public FoundItemResponse toResponse(FoundItems foundItem) {
        if (foundItem == null) return null;

        FoundItemResponse foundItemResponse = new FoundItemResponse();
        foundItemResponse.setId(foundItem.getId());
        foundItemResponse.setName(foundItem.getName());
        foundItemResponse.setDescription(foundItem.getDescription());
        foundItemResponse.setLocation(foundItem.getLocation());
        foundItemResponse.setFoundDate(foundItem.getFoundDate());
        foundItemResponse.setFoundByUserId(foundItem.getFoundByUserId());
        foundItemResponse.setReportedDate(foundItem.getReportedDate());
        foundItemResponse.setStatus(foundItem.getStatus());
        foundItemResponse.setImageUrl(foundItem.getImageUrl());

        // Set category name from the Category entity
        if (foundItem.getCategory() != null) {
            foundItemResponse.setCategoryName(foundItem.getCategory().getName());
        }

        return foundItemResponse;
    }
}