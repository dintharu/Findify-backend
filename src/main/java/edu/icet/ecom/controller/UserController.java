//package edu.icet.ecom.controller;
//
//import edu.icet.ecom.model.dto.response.UserResponse;
//import edu.icet.ecom.service.custom.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/users")
//@RequiredArgsConstructor
//@CrossOrigin
//public class UserController {
//
//    private final UserService userService;
//
////    @GetMapping("/{id}")
////    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
////        UserResponse user = userService.getUserById(id);
////        return ResponseEntity.ok(user);
////    }
//
//    @GetMapping("/api/users/{userId}")
//    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
//        // Implementation to return user info
//        try {
//            UserResponse user = userService.getUserById(userId);
//            return ResponseEntity.ok(user);
//        } catch (Exception e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @GetMapping("/email/{email}")
//    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
//        UserResponse user = userService.getUserByEmail(email);
//        return ResponseEntity.ok(user);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<UserResponse>> getAllUsers() {
//        List<UserResponse> users = userService.getAllUsers();
//        return ResponseEntity.ok(users);
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<String> healthCheck() {
//        return ResponseEntity.ok("User service is running");
//    }
//}


package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.response.UserResponse;
import edu.icet.ecom.service.custom.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"},
        allowCredentials = "true",
        allowedHeaders = {"*"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class UserController {

    private final UserService userService;

    // FIXED: Removed duplicate /api/users from path
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        try {
            UserResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("User service is running");
    }
}