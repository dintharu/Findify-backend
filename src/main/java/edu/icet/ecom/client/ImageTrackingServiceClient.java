package edu.icet.ecom.client;

import edu.icet.ecom.model.dto.ItemDto;
import edu.icet.ecom.model.dto.SimilarityResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "image-tracking-service", url = "http://localhost:8084")
public interface ImageTrackingServiceClient {

    @PostMapping("/api/image-similarity/check-similarity")
    SimilarityResult checkSimilarity(
            @RequestParam String imageUrl1,
            @RequestParam String imageUrl2);

    @PostMapping("/api/image-similarity/find-matches")
    ResponseEntity<String> findMatches(@RequestBody ItemDto newItem);

    @GetMapping("/api/image-similarity/health")
    ResponseEntity<String> healthCheck();
}