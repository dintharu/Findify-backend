package edu.icet.ecom.service.custom;

import edu.icet.ecom.Enum.UserStatus;
import edu.icet.ecom.model.dto.response.UserResponse;
import edu.icet.ecom.model.entity.User;

import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getUsersByStatus(UserStatus status);
    UserResponse updateUserStatus(Long id, UserStatus status);
    void deleteUser(Long id);
    User getCurrentUser();

}
