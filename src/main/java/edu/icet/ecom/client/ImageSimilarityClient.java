package edu.icet.ecom.client;


import edu.icet.ecom.model.dto.ImageSimilarityItemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "image-tracking-service", url = "http://localhost:8084")
public interface ImageSimilarityClient {
    @PostMapping("/api/image-similarity/find-matches")
    ResponseEntity<String> findMatches(@RequestBody ImageSimilarityItemDto itemDto);
}
