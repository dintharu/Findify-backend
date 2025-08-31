package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.ItemDto;
import edu.icet.ecom.model.dto.SimilarityResult;
import edu.icet.ecom.service.ImageSimilarityService;
import edu.icet.ecom.service.ItemMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image-similarity")
@CrossOrigin(origins = "*")
public class ImageSimilarityController {

    @Autowired
    private ImageSimilarityService imageSimilarityService;

    @Autowired
    private ItemMatchingService itemMatchingService;

    @PostMapping("/check-similarity")
    public ResponseEntity<SimilarityResult> checkSimilarity(
            @RequestParam String imageUrl1,
            @RequestParam String imageUrl2) {

        double similarity = imageSimilarityService.calculateSimilarity(imageUrl1, imageUrl2);
        SimilarityResult result = new SimilarityResult("", "", similarity);

        return ResponseEntity.ok(result);
    }


    @PostMapping("/find-matches")
    public ResponseEntity<String> findMatches(@RequestBody ItemDto newItem) {
        itemMatchingService.findMatches(newItem);
        return ResponseEntity.ok("Match checking completed");
    }


    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Image Similarity Service is running");
    }
}