// SimilarityResult.java
package edu.icet.ecom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityResult {
    private double similarityScore;
    private boolean isMatch;
    private String confidence;
    private String message;
}