package edu.icet.ecom.service.custom;

import edu.icet.ecom.model.dto.request.CreateLostItemRequest;
import edu.icet.ecom.model.dto.response.LostItemResponse;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface LostItemService {
    LostItemResponse createLostItem(CreateLostItemRequest request, Long userId);
    LostItemResponse getLostItemById(Long id);
    List<LostItemResponse> getAllLostItems(Pageable pageable);
    void deleteLostItem(Long id);
LostItemResponse updateLostItem(Long id, CreateLostItemRequest request, Long userId);
List<LostItemResponse> searchLostItems(String query, Pageable pageable);


}
