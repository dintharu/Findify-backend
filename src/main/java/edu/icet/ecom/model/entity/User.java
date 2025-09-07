//package edu.icet.ecom.model.entity;
//
//import edu.icet.ecom.Enum.UserRole;
//import edu.icet.ecom.Enum.UserStatus;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.List;
//
//@Entity
//@Table(name = "users")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder // Added missing @Builder annotation
//public class User implements UserDetails {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String fullname;
//
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    @Column(nullable = false)
//    private String password;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    @Builder.Default // Use Builder.Default for default values
//    private UserRole role = UserRole.STUDENT;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    @Builder.Default // Use Builder.Default for default values
//    private UserStatus status = UserStatus.ACTIVE;
//
//    @Column(name = "created_at")
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    // UserDetails implementation methods
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
//    }
//
//    @Override
//    public String getUsername() {
//        return email; // Using email as username
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return status == UserStatus.ACTIVE;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return status == UserStatus.ACTIVE;
//    }
//}

package edu.icet.ecom.model.entity;

import edu.icet.ecom.Enum.UserRole;
import edu.icet.ecom.Enum.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullname;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // CRITICAL: UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    // Getter methods for custom fields
    public String getFullname() {
        return fullname;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }
}