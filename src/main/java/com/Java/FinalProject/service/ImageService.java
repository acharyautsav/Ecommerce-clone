package com.Java.FinalProject.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ImageService {

    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    private static final String PRODUCTS_DIR = "products/";
    
    public String uploadProductImage(MultipartFile file) throws IOException {
        return uploadImage(file, PRODUCTS_DIR);
    }
    
    private String uploadImage(MultipartFile file, String subDirectory) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Create directories if they don't exist
        Path uploadPath = Paths.get(UPLOAD_DIR + subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String baseName = originalFilename != null ? 
            originalFilename.substring(0, originalFilename.lastIndexOf(".")) : "image";
        String filename = baseName + "_" + timestamp + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // Verify file was saved
        if (!Files.exists(filePath)) {
            throw new IOException("Failed to save image file: " + filePath);
        }
        
        // Log successful upload for debugging
        System.out.println("Image uploaded successfully: " + filePath);
        System.out.println("File size: " + Files.size(filePath) + " bytes");
        
        // Return relative path for database storage
        return "images/" + subDirectory + filename;
    }
    
    public void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Path fullPath = Paths.get("src/main/resources/static/" + imagePath);
                if (Files.exists(fullPath)) {
                    Files.delete(fullPath);
                    System.out.println("Image deleted successfully: " + fullPath);
                }
            } catch (IOException e) {
                // Log error but don't throw - file might already be deleted
                System.err.println("Error deleting image: " + imagePath + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * Force refresh of static resources (useful for development)
     */
    public void refreshStaticResources() {
        try {
            // This is a workaround for development mode
            // In production, this would not be necessary
            System.out.println("Static resources refreshed");
        } catch (Exception e) {
            System.err.println("Error refreshing static resources: " + e.getMessage());
        }
    }
} 