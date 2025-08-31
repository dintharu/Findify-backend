package edu.icet.ecom.model.dto.request;


import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Builder
public class LoginRequest {

    @NotBlank(message = "Username or email cannot be empty")
    private String email; // Can be username or email

    @NotBlank(message = "Password cannot be empty")
    private String password;

}
