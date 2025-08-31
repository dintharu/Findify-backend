package edu.icet.ecom.model.dto.internel;

import edu.icet.ecom.Enum.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
}
