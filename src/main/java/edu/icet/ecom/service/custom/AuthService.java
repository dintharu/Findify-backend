package edu.icet.ecom.service.custom;

import edu.icet.ecom.model.dto.request.LoginRequest;
import edu.icet.ecom.model.dto.request.UserRegistrationRequest;
import edu.icet.ecom.model.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(UserRegistrationRequest request);
    AuthResponse login(LoginRequest request);

}
