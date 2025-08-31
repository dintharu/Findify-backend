package edu.icet.ecom.service.custom;

import edu.icet.ecom.model.dto.request.CreateFoundItemRequest;
import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.dto.response.LostItemResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FoundItemService {
    FoundItemResponse createFoundItem(CreateFoundItemRequest request, Long userId);
    FoundItemResponse getFoundItemById(Long id);
    List<FoundItemResponse> getAllFoundItems(Pageable pageable);
    void deleteFoundItem(Long id);
    FoundItemResponse updateFoundItem(Long id, CreateFoundItemRequest request, Long userId);
    List<FoundItemResponse> searchFoundItems(String query, Pageable pageable);
}
