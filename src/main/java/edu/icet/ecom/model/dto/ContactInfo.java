package edu.icet.ecom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactInfo {
    private Long id;        // Real user ID - CRITICAL for chat functionality
    private String name;    // Real user name from UserResponse
    private String email;   // Real user email - CRITICAL for user identification
    private String phone;   // Optional phone number

    // Constructor for easy creation from UserResponse
    public ContactInfo(Long userId, String fullName, String userEmail) {
        this.id = userId;
        this.name = fullName;
        this.email = userEmail;
        this.phone = ""; // Default empty phone
    }

    // Helper method to validate if this contact info is complete
    public boolean isValid() {
        return id != null &&
                name != null && !name.trim().isEmpty() &&
                email != null && !email.trim().isEmpty();
    }

    // Helper method for debugging
    @Override
    public String toString() {
        return String.format("ContactInfo{id=%d, name='%s', email='%s', phone='%s'}",
                id, name, email, phone);
    }
}