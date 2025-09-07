//package edu.icet.ecom.dto;
//
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class UserDto {
//
//    private Long id;
//    private String username;
//    private String email;
//    private String firstName;
//    private String lastName;
//    private String role;
//    private String profilePicture;
//    private Boolean isActive;
//
//}


package edu.icet.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;

    // FIXED: Match the user service response format
    private String fullName;        // Changed from firstName/lastName
    private String firstName;       // Keep for backward compatibility
    private String lastName;        // Keep for backward compatibility

    private String role;
    private String status;
    private String profilePicture;
    private Boolean isActive;

    // FIXED: Helper methods to handle both formats
    public String getFirstName() {
        if (firstName != null) {
            return firstName;
        }
        // Extract from fullName if available
        if (fullName != null && fullName.contains(" ")) {
            return fullName.split(" ")[0];
        }
        return fullName;
    }

    public String getLastName() {
        if (lastName != null) {
            return lastName;
        }
        // Extract from fullName if available
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ");
            return parts.length > 1 ? parts[parts.length - 1] : "";
        }
        return "";
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }

        String first = getFirstName();
        String last = getLastName();

        if (first != null && last != null) {
            return (first + " " + last).trim();
        } else if (first != null) {
            return first;
        } else if (username != null) {
            return username;
        }

        return "User " + id;
    }
}