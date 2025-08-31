package edu.icet.ecom.client;

import edu.icet.ecom.model.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8080")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    UserResponse getUserById(@PathVariable Long id);

    @GetMapping("/api/users/email/{email}")
    UserResponse getUserByEmail(@PathVariable String email);

    @GetMapping("/api/users/health")
    String healthCheck();
}