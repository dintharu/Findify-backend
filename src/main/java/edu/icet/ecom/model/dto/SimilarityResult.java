package edu.icet.ecom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityResult {
    private String imageUrl1;
    private String imageUrl2;
    private double similarityScore;
    private boolean isMatch;
    private String matchReason;

    // Constructor for basic similarity result
    public SimilarityResult(String imageUrl1, String imageUrl2, double similarityScore) {
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
        this.similarityScore = similarityScore;
        this.isMatch = similarityScore >= 0.65; // Default threshold
        this.matchReason = isMatch ? "Images are similar enough for matching" : "Images are not similar enough";
    }

    // Helper method to check if similarity is above threshold
    public boolean isAboveThreshold(double threshold) {
        return this.similarityScore >= threshold;
    }

    // Helper method to get similarity percentage
    public double getSimilarityPercentage() {
        return this.similarityScore * 100;
    }
}