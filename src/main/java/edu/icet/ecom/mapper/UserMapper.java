//package edu.icet.ecom.mapper;
//
//import edu.icet.ecom.Enum.UserRole;
//import edu.icet.ecom.Enum.UserStatus;
//import edu.icet.ecom.model.dto.request.UserRegistrationRequest;
//import edu.icet.ecom.model.dto.response.UserResponse;
//import edu.icet.ecom.model.entity.User;
//import org.modelmapper.ModelMapper;
//import org.springframework.boot.Banner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//public class UserMapper {
//    public UserResponse toResponse(User user){
//        if(user == null)return null;
//        return UserResponse.builder()
//                .id(user.getId())
//                .email(user.getEmail())
//                .fullName(user.getFullname())
//                .role(user.getRole())
//                .status(user.getStatus())
//                .createdAt(user.getCreatedAt())
//                .updatedAt(user.getUpdatedAt())
//                .build();
//    }
//
//    public User toEntity(UserRegistrationRequest request) {
//        if (request == null) return null;
//        return User.builder()
//                .email(request.getEmail())
//                .password(request.getPassword()) // Password will be encoded in service
//                .fullname(request.getFullname())
//                .role(UserRole.STUDENT)// Default role for new registrations
//                .status(UserStatus.ACTIVE)
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//    // You can add updateEntityFromRequest for PUT/PATCH operations
//}


package edu.icet.ecom.mapper;

import edu.icet.ecom.Enum.UserRole;
import edu.icet.ecom.Enum.UserStatus;
import edu.icet.ecom.model.dto.request.UserRegistrationRequest;
import edu.icet.ecom.model.dto.response.UserResponse;
import edu.icet.ecom.model.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullname()) // Fixed: Use getFullname()
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity(UserRegistrationRequest request) {
        if (request == null) return null;
        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // Password will be encoded in service
                .fullname(request.getFullname())
                .role(UserRole.STUDENT) // Default role for new registrations
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()) // Add updatedAt
                .build();
    }
}