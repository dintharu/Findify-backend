package edu.icet.ecom.service.custom;

import edu.icet.ecom.model.dto.response.FoundItemResponse;
import edu.icet.ecom.model.dto.response.LostItemResponse;

public interface ImageSimilarityIntegrationService {

    void checkForMatchesAfterLostItemCreated(LostItemResponse lostItem);
    void checkForMatchesAfterFoundItemCreated(FoundItemResponse foundItem);
  //  String getUserEmail(Long userId);

}
