//package edu.icet.ecom.client;
//
//import edu.icet.ecom.dto.UserDto;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//@FeignClient(name = "user-service", url = "http://localhost:8080")
//public interface UserServiceClient {
//
////    @GetMapping("/api/users/{id}")
////    UserDto getUserById(@PathVariable Long id);
////
//////    @GetMapping("/api/users/email/{email}")
//////    UserDto getUserByEmail(@PathVariable String email);
////
////    @GetMapping("/api/users/email/{email}")
////    UserDto getUserByEmail(@PathVariable("email") String email);
////
////    @GetMapping("/api/users/health")
////    String healthCheck();
//
//@GetMapping("/api/users/{id}")
//UserDto getUserById(@PathVariable("id") Long id);
//
//    @GetMapping("/api/users/email/{email}")
//    UserDto getUserByEmail(@PathVariable("email") String email);
//
//    @GetMapping("/api/users/health")
//    String healthCheck();
//
//
//
//
//}

package edu.icet.ecom.client;

import edu.icet.ecom.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

// FIXED: Add authorization header support and correct configuration
@FeignClient(
        name = "user-service",
        url = "http://localhost:8080",
        configuration = UserServiceClientConfig.class
)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(
            @PathVariable("email") String email,
            @RequestHeader("Authorization") String authorization
    );

    @GetMapping("/api/users/health")
    String healthCheck();

    // FIXED: Add overloaded methods without authorization for internal use
    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/email/{email}")
    UserDto getUserByEmail(@PathVariable("email") String email);
}