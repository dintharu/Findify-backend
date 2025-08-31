package edu.icet.ecom.service;

import edu.icet.ecom.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.user-service}")
    private String userServiceUrl;

    public UserDto getUserById(Long userId) {
        try {
            log.debug("Fetching user info for userId: {}", userId);

            WebClient webClient = webClientBuilder
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1 * 1024 * 1024))
                    .build();

            Mono<UserDto> userMono = webClient
                    .get()
                    .uri(userServiceUrl + "/api/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .timeout(Duration.ofSeconds(5));

            UserDto user = userMono.block();
            log.debug("Successfully fetched user: {}", user != null ? user.getUsername() : "null");
            return user;

        } catch (Exception e) {
            log.error("Error fetching user info for userId: {}, Error: {}", userId, e.getMessage());

            // Return default user info in case of failure
            UserDto defaultUser = new UserDto();
            defaultUser.setId(userId);
            defaultUser.setUsername("User" + userId);
            defaultUser.setFirstName("Unknown");
            defaultUser.setLastName("User");
            defaultUser.setEmail("user" + userId + "@example.com");
            return defaultUser;
        }
    }

    public String getUserDisplayName(Long userId) {
        try {
            UserDto user = getUserById(userId);
            if (user != null && user.getFirstName() != null) {
                return user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            }
            return "User " + userId;
        } catch (Exception e) {
            log.warn("Could not get display name for user {}: {}", userId, e.getMessage());
            return "User " + userId;
        }
    }
}