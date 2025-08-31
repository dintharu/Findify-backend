package edu.icet.ecom.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

@Service
public class ImageSimilarityService {

    private static final double SIMILARITY_THRESHOLD = 0.65; // Lowered threshold for better matching

    public double calculateSimilarity(String imageUrl1, String imageUrl2) {
        try {
            System.out.println("=== IMAGE SIMILARITY CALCULATION ===");
            System.out.println("Image 1 URL type: " + getUrlType(imageUrl1));
            System.out.println("Image 2 URL type: " + getUrlType(imageUrl2));

            BufferedImage img1 = downloadImage(imageUrl1);
            BufferedImage img2 = downloadImage(imageUrl2);

            if (img1 == null || img2 == null) {
                System.err.println("One or both images failed to load");
                return 0.0;
            }

            System.out.println("Images loaded successfully");
            System.out.println("Image 1 size: " + img1.getWidth() + "x" + img1.getHeight());
            System.out.println("Image 2 size: " + img2.getWidth() + "x" + img2.getHeight());

            // Multiple similarity methods combined
            double histogramSimilarity = compareColorHistograms(img1, img2);
            double structuralSimilarity = compareStructuralFeatures(img1, img2);
            double pixelSimilarity = comparePixelSimilarity(img1, img2);
            double hashSimilarity = comparePerceptualHashes(img1, img2);

            System.out.println("Histogram similarity: " + String.format("%.3f", histogramSimilarity));
            System.out.println("Structural similarity: " + String.format("%.3f", structuralSimilarity));
            System.out.println("Pixel similarity: " + String.format("%.3f", pixelSimilarity));
            System.out.println("Hash similarity: " + String.format("%.3f", hashSimilarity));

            // Weighted combination of all methods
            double finalSimilarity = (histogramSimilarity * 0.3) +
                    (structuralSimilarity * 0.2) +
                    (pixelSimilarity * 0.2) +
                    (hashSimilarity * 0.3);

            System.out.println("Final combined similarity: " + String.format("%.3f", finalSimilarity));
            System.out.println("====================================");

            return finalSimilarity;

        } catch (Exception e) {
            System.err.println("Error calculating similarity: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    private String getUrlType(String imageUrl) {
        if (imageUrl.startsWith("data:")) {
            return "Base64 Data URL";
        } else if (imageUrl.startsWith("http")) {
            return "HTTP URL";
        } else {
            return "Unknown URL type";
        }
    }

    private BufferedImage downloadImage(String imageUrl) {
        try {
            BufferedImage originalImage;

            if (imageUrl.startsWith("data:")) {
                // Handle base64 data URLs
                originalImage = decodeBase64Image(imageUrl);
            } else {
                // Handle regular URLs
                URL url = new URL(imageUrl);
                originalImage = ImageIO.read(url);
            }

            if (originalImage == null) {
                System.err.println("Failed to decode image");
                return null;
            }

            // Resize for consistent comparison
            return resizeImage(originalImage, 200, 200);

        } catch (Exception e) {
            System.err.println("Error downloading/processing image: " + e.getMessage());
            return null;
        }
    }

    private BufferedImage decodeBase64Image(String base64Image) {
        try {
            // Remove data URL prefix (e.g., "data:image/jpeg;base64,")
            String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);

            // Decode base64 string
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // Convert to BufferedImage
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            return ImageIO.read(bis);

        } catch (Exception e) {
            System.err.println("Error decoding base64 image: " + e.getMessage());
            return null;
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }

    // Method 1: Enhanced Color Histogram Comparison
    private double compareColorHistograms(BufferedImage img1, BufferedImage img2) {
        int[][][] hist1 = calculateRGBHistogram(img1);
        int[][][] hist2 = calculateRGBHistogram(img2);

        double rCorrelation = calculateHistogramCorrelation(hist1[0], hist2[0]);
        double gCorrelation = calculateHistogramCorrelation(hist1[1], hist2[1]);
        double bCorrelation = calculateHistogramCorrelation(hist1[2], hist2[2]);

        return (rCorrelation + gCorrelation + bCorrelation) / 3.0;
    }

    private int[][][] calculateRGBHistogram(BufferedImage image) {
        int[][][] histogram = new int[3][256][1]; // RGB channels

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                histogram[0][color.getRed()][0]++;
                histogram[1][color.getGreen()][0]++;
                histogram[2][color.getBlue()][0]++;
            }
        }

        return histogram;
    }

    private double calculateHistogramCorrelation(int[][] hist1, int[][] hist2) {
        double sum1 = 0, sum2 = 0, sum1Sq = 0, sum2Sq = 0, pSum = 0;
        int n = hist1.length;

        for (int i = 0; i < n; i++) {
            sum1 += hist1[i][0];
            sum2 += hist2[i][0];
            sum1Sq += hist1[i][0] * hist1[i][0];
            sum2Sq += hist2[i][0] * hist2[i][0];
            pSum += hist1[i][0] * hist2[i][0];
        }

        double num = pSum - (sum1 * sum2 / n);
        double den = Math.sqrt((sum1Sq - sum1 * sum1 / n) * (sum2Sq - sum2 * sum2 / n));

        if (den == 0) return 0;

        double correlation = num / den;
        return Math.abs(correlation); // Return absolute value
    }

    // Method 2: Structural Feature Comparison
    private double compareStructuralFeatures(BufferedImage img1, BufferedImage img2) {
        double[][] edges1 = getSobelEdges(img1);
        double[][] edges2 = getSobelEdges(img2);

        return compareEdgeFeatures(edges1, edges2);
    }

    private double[][] getSobelEdges(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] edges = new double[width][height];

        // Convert to grayscale first
        int[][] gray = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = new Color(image.getRGB(x, y));
                gray[x][y] = (int)(0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
            }
        }

        // Apply Sobel operators
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int gx = -gray[x-1][y-1] - 2*gray[x-1][y] - gray[x-1][y+1] +
                        gray[x+1][y-1] + 2*gray[x+1][y] + gray[x+1][y+1];

                int gy = -gray[x-1][y-1] - 2*gray[x][y-1] - gray[x+1][y-1] +
                        gray[x-1][y+1] + 2*gray[x][y+1] + gray[x+1][y+1];

                edges[x][y] = Math.sqrt(gx*gx + gy*gy);
            }
        }

        return edges;
    }

    private double compareEdgeFeatures(double[][] edges1, double[][] edges2) {
        int width = Math.min(edges1.length, edges2.length);
        int height = Math.min(edges1[0].length, edges2[0].length);

        double totalDiff = 0;
        double maxPossibleDiff = 255.0 * width * height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                totalDiff += Math.abs(edges1[x][y] - edges2[x][y]);
            }
        }

        return 1.0 - (totalDiff / maxPossibleDiff);
    }

    // Method 3: Direct Pixel Comparison
    private double comparePixelSimilarity(BufferedImage img1, BufferedImage img2) {
        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());

        double totalDiff = 0;
        double maxDiff = 255.0 * 3 * width * height; // RGB channels

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c1 = new Color(img1.getRGB(x, y));
                Color c2 = new Color(img2.getRGB(x, y));

                totalDiff += Math.abs(c1.getRed() - c2.getRed());
                totalDiff += Math.abs(c1.getGreen() - c2.getGreen());
                totalDiff += Math.abs(c1.getBlue() - c2.getBlue());
            }
        }

        return 1.0 - (totalDiff / maxDiff);
    }

    // Method 4: Perceptual Hash Comparison
    private double comparePerceptualHashes(BufferedImage img1, BufferedImage img2) {
        String hash1 = calculatePerceptualHash(img1);
        String hash2 = calculatePerceptualHash(img2);

        return compareHashes(hash1, hash2);
    }

    private String calculatePerceptualHash(BufferedImage image) {
        // Resize to 8x8 for hash
        BufferedImage small = resizeImage(image, 8, 8);

        // Convert to grayscale and get average
        int[] pixels = new int[64];
        int sum = 0;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Color c = new Color(small.getRGB(x, y));
                int gray = (int)(0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
                pixels[x * 8 + y] = gray;
                sum += gray;
            }
        }

        int average = sum / 64;

        // Build hash string
        StringBuilder hash = new StringBuilder();
        for (int pixel : pixels) {
            hash.append(pixel >= average ? "1" : "0");
        }

        return hash.toString();
    }

    private double compareHashes(String hash1, String hash2) {
        if (hash1.length() != hash2.length()) return 0.0;

        int matches = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) == hash2.charAt(i)) {
                matches++;
            }
        }

        return (double) matches / hash1.length();
    }

    public boolean isSimilarEnough(double similarity) {
        boolean isMatch = similarity >= SIMILARITY_THRESHOLD;
        System.out.println("Similarity threshold check: " + String.format("%.2f", similarity * 100) +
                "% >= " + String.format("%.1f", SIMILARITY_THRESHOLD * 100) + "% = " + isMatch);
        return isMatch;
    }
}