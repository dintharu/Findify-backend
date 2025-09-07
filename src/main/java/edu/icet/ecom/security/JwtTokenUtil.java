////////package edu.icet.ecom.security;
////////
////////import io.jsonwebtoken.Claims;
////////import io.jsonwebtoken.ExpiredJwtException;
////////import io.jsonwebtoken.Jwts;
////////import io.jsonwebtoken.MalformedJwtException;
////////import io.jsonwebtoken.UnsupportedJwtException;
////////import io.jsonwebtoken.security.Keys;
////////import io.jsonwebtoken.security.SecurityException;
////////import lombok.extern.slf4j.Slf4j;
////////import org.springframework.beans.factory.annotation.Value;
////////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////////import org.springframework.security.core.Authentication;
////////import org.springframework.security.core.GrantedAuthority;
////////import org.springframework.security.core.authority.SimpleGrantedAuthority;
////////import org.springframework.stereotype.Component;
////////
////////import javax.crypto.SecretKey;
////////import java.util.Arrays;
////////import java.util.Collection;
////////import java.util.stream.Collectors;
////////
////////@Component
////////@Slf4j
////////public class JwtTokenUtil {
////////
////////    private final SecretKey key;
////////    private final int jwtExpirationInMs;
////////
////////    public JwtTokenUtil(@Value("${application.security.jwt.secret-key}") String jwtSecret,
////////                        @Value("${application.security.jwt.expiration}") int jwtExpirationInMs) {
////////        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
////////        this.jwtExpirationInMs = jwtExpirationInMs;
////////    }
////////
////////    public boolean validateToken(String token) {
////////        try {
////////            Jwts.parser()
////////                    .setSigningKey(key)
////////                    .build()
////////                    .parseClaimsJws(token);
////////            return true;
////////        } catch (ExpiredJwtException e) {
////////            log.error("JWT token is expired: {}", e.getMessage());
////////        } catch (UnsupportedJwtException e) {
////////            log.error("JWT token is unsupported: {}", e.getMessage());
////////        } catch (MalformedJwtException e) {
////////            log.error("Invalid JWT token: {}", e.getMessage());
////////        } catch (SecurityException e) {
////////            log.error("Invalid JWT signature: {}", e.getMessage());
////////        } catch (IllegalArgumentException e) {
////////            log.error("JWT claims string is empty: {}", e.getMessage());
////////        }
////////        return false;
////////    }
////////
////////    public Long getUserIdFromToken(String token) {
////////        try {
////////            Claims claims = Jwts.parser()
////////                    .setSigningKey(key)
////////                    .build()
////////                    .parseClaimsJws(token)
////////                    .getBody();
////////
////////            return claims.get("userId", Long.class);
////////        } catch (Exception e) {
////////            log.error("Error extracting user ID from token: {}", e.getMessage());
////////            throw new RuntimeException("Invalid token");
////////        }
////////    }
////////
////////    public String getUsernameFromToken(String token) {
////////        try {
////////            Claims claims = Jwts.parser()
////////                    .setSigningKey(key)
////////                    .build()
////////                    .parseClaimsJws(token)
////////                    .getBody();
////////
////////            return claims.getSubject();
////////        } catch (Exception e) {
////////            log.error("Error extracting username from token: {}", e.getMessage());
////////            throw new RuntimeException("Invalid token");
////////        }
////////    }
////////
////////    public Authentication getAuthentication(String token) {
////////        try {
////////            Claims claims = Jwts.parser()
////////                    .setSigningKey(key)
////////                    .build()
////////                    .parseClaimsJws(token)
////////                    .getBody();
////////
////////            String username = claims.getSubject();
////////            String authorities = claims.get("authorities", String.class);
////////
////////            Collection<? extends GrantedAuthority> auths = authorities != null && !authorities.isEmpty() ?
////////                    Arrays.stream(authorities.split(","))
////////                            .map(SimpleGrantedAuthority::new)
////////                            .collect(Collectors.toList()) :
////////                    Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
////////
////////            return new UsernamePasswordAuthenticationToken(username, token, auths);
////////        } catch (Exception e) {
////////            log.error("Error creating authentication from token: {}", e.getMessage());
////////            throw new RuntimeException("Invalid token");
////////        }
////////    }
////////}
//////
//////package edu.icet.ecom.security;
//////
//////import io.jsonwebtoken.Claims;
//////import io.jsonwebtoken.ExpiredJwtException;
//////import io.jsonwebtoken.Jwts;
//////import io.jsonwebtoken.MalformedJwtException;
//////import io.jsonwebtoken.UnsupportedJwtException;
//////import io.jsonwebtoken.security.Keys;
//////import io.jsonwebtoken.security.SecurityException;
//////import lombok.extern.slf4j.Slf4j;
//////import org.springframework.beans.factory.annotation.Value;
//////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//////import org.springframework.security.core.Authentication;
//////import org.springframework.security.core.GrantedAuthority;
//////import org.springframework.security.core.authority.SimpleGrantedAuthority;
//////import org.springframework.stereotype.Component;
//////
//////import javax.crypto.SecretKey;
//////import java.util.Arrays;
//////import java.util.Collection;
//////import java.util.stream.Collectors;
//////
//////@Component
//////@Slf4j
//////public class JwtTokenUtil {
//////
//////    private final SecretKey key;
//////    private final int jwtExpirationInMs;
//////
//////    public JwtTokenUtil(@Value("${application.security.jwt.secret-key}") String jwtSecret,
//////                        @Value("${application.security.jwt.expiration}") int jwtExpirationInMs) {
//////        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
//////        this.jwtExpirationInMs = jwtExpirationInMs;
//////    }
//////
//////    public boolean validateToken(String token) {
//////        try {
//////            Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token);
//////            return true;
//////        } catch (ExpiredJwtException e) {
//////            log.error("JWT token is expired: {}", e.getMessage());
//////        } catch (UnsupportedJwtException e) {
//////            log.error("JWT token is unsupported: {}", e.getMessage());
//////        } catch (MalformedJwtException e) {
//////            log.error("Invalid JWT token: {}", e.getMessage());
//////        } catch (SecurityException e) {
//////            log.error("Invalid JWT signature: {}", e.getMessage());
//////        } catch (IllegalArgumentException e) {
//////            log.error("JWT claims string is empty: {}", e.getMessage());
//////        }
//////        return false;
//////    }
//////
//////    public Long getUserIdFromToken(String token) {
//////        try {
//////            Claims claims = Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token)
//////                    .getBody();
//////
//////            // FIXED: Handle both possible claim names from user service
//////            Object userIdClaim = claims.get("userId");
//////            if (userIdClaim != null) {
//////                if (userIdClaim instanceof Number) {
//////                    return ((Number) userIdClaim).longValue();
//////                } else if (userIdClaim instanceof String) {
//////                    return Long.parseLong((String) userIdClaim);
//////                }
//////            }
//////
//////            // Fallback: try to get from subject as ID
//////            String subject = claims.getSubject();
//////            if (subject != null && subject.matches("\\d+")) {
//////                return Long.parseLong(subject);
//////            }
//////
//////            log.warn("No valid userId found in token claims: {}", claims.keySet());
//////            throw new RuntimeException("User ID not found in token");
//////
//////        } catch (Exception e) {
//////            log.error("Error extracting user ID from token: {}", e.getMessage());
//////            throw new RuntimeException("Invalid token: " + e.getMessage());
//////        }
//////    }
//////
//////    public String getUsernameFromToken(String token) {
//////        try {
//////            Claims claims = Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token)
//////                    .getBody();
//////
//////            return claims.getSubject();
//////        } catch (Exception e) {
//////            log.error("Error extracting username from token: {}", e.getMessage());
//////            throw new RuntimeException("Invalid token");
//////        }
//////    }
//////
//////    // FIXED: Enhanced method to extract user full name
//////    public String getFullNameFromToken(String token) {
//////        try {
//////            Claims claims = Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token)
//////                    .getBody();
//////
//////            // Try multiple possible claim names
//////            String fullName = claims.get("fullName", String.class);
//////            if (fullName != null) {
//////                return fullName;
//////            }
//////
//////            fullName = claims.get("fullname", String.class);
//////            if (fullName != null) {
//////                return fullName;
//////            }
//////
//////            // Fallback to username
//////            return claims.getSubject();
//////
//////        } catch (Exception e) {
//////            log.error("Error extracting full name from token: {}", e.getMessage());
//////            return null;
//////        }
//////    }
//////
//////    // FIXED: Enhanced method to extract user role
//////    public String getRoleFromToken(String token) {
//////        try {
//////            Claims claims = Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token)
//////                    .getBody();
//////
//////            return claims.get("role", String.class);
//////        } catch (Exception e) {
//////            log.error("Error extracting role from token: {}", e.getMessage());
//////            return "STUDENT"; // Default role
//////        }
//////    }
//////
//////    public Authentication getAuthentication(String token) {
//////        try {
//////            Claims claims = Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token)
//////                    .getBody();
//////
//////            String username = claims.getSubject();
//////
//////            // FIXED: Enhanced authority extraction
//////            String role = claims.get("role", String.class);
//////            Collection<? extends GrantedAuthority> auths;
//////
//////            if (role != null && !role.isEmpty()) {
//////                // Ensure role has ROLE_ prefix
//////                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
//////                auths = Arrays.asList(new SimpleGrantedAuthority(authority));
//////            } else {
//////                auths = Arrays.asList(new SimpleGrantedAuthority("ROLE_STUDENT"));
//////            }
//////
//////            log.debug("Created authentication for user: {} with authorities: {}", username, auths);
//////            return new UsernamePasswordAuthenticationToken(username, token, auths);
//////
//////        } catch (Exception e) {
//////            log.error("Error creating authentication from token: {}", e.getMessage());
//////            throw new RuntimeException("Invalid token");
//////        }
//////    }
//////
//////    // FIXED: Debug method to inspect token claims
//////    public void debugTokenClaims(String token) {
//////        try {
//////            Claims claims = Jwts.parser()
//////                    .setSigningKey(key)
//////                    .build()
//////                    .parseClaimsJws(token)
//////                    .getBody();
//////
//////            log.info("=== TOKEN CLAIMS DEBUG ===");
//////            log.info("Subject: {}", claims.getSubject());
//////            log.info("Issued At: {}", claims.getIssuedAt());
//////            log.info("Expiration: {}", claims.getExpiration());
//////            log.info("All Claims:");
//////            claims.forEach((key, value) -> log.info("  {}: {} ({})", key, value, value != null ? value.getClass().getSimpleName() : "null"));
//////            log.info("=== END TOKEN CLAIMS ===");
//////
//////        } catch (Exception e) {
//////            log.error("Error debugging token claims: {}", e.getMessage());
//////        }
//////    }
//////}
////
////package edu.icet.ecom.security;
////
////import io.jsonwebtoken.Claims;
////import io.jsonwebtoken.ExpiredJwtException;
////import io.jsonwebtoken.Jwts;
////import io.jsonwebtoken.MalformedJwtException;
////import io.jsonwebtoken.UnsupportedJwtException;
////import io.jsonwebtoken.security.Keys;
////import io.jsonwebtoken.security.SecurityException;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
////import org.springframework.security.core.Authentication;
////import org.springframework.security.core.GrantedAuthority;
////import org.springframework.security.core.authority.SimpleGrantedAuthority;
////import org.springframework.stereotype.Component;
////
////import javax.crypto.SecretKey;
////import java.util.Arrays;
////import java.util.Collection;
////
////@Component
////@Slf4j
////public class JwtTokenUtil {
////
////    private final SecretKey key;
////
////    public JwtTokenUtil(@Value("${application.security.jwt.secret-key}") String jwtSecret) {
////        // CRITICAL: Use the exact same key format as your user service
////        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
////        log.info("JWT TokenUtil initialized with key length: {}", jwtSecret.length());
////    }
////
////    public boolean validateToken(String token) {
////        try {
////            log.debug("Validating token: {}...", token.substring(0, Math.min(20, token.length())));
////
////            Claims claims = Jwts.parser()
////                    .setSigningKey(key)  // FIXED: Use setSigningKey for compatibility
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody();
////
////            log.debug("Token validation successful. Claims: {}", claims.keySet());
////            return true;
////
////        } catch (ExpiredJwtException e) {
////            log.error("JWT token is expired: {}", e.getMessage());
////        } catch (UnsupportedJwtException e) {
////            log.error("JWT token is unsupported: {}", e.getMessage());
////        } catch (MalformedJwtException e) {
////            log.error("Invalid JWT token: {}", e.getMessage());
////        } catch (SecurityException e) {
////            log.error("Invalid JWT signature: {}", e.getMessage());
////        } catch (IllegalArgumentException e) {
////            log.error("JWT claims string is empty: {}", e.getMessage());
////        } catch (Exception e) {
////            log.error("Unexpected JWT validation error: {}", e.getMessage());
////        }
////        return false;
////    }
////
////    public Long getUserIdFromToken(String token) {
////        try {
////            Claims claims = Jwts.parser()
////                    .setSigningKey(key)
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody();
////
////            // CRITICAL FIX: Handle the exact claim format from your user service
////            Object userIdClaim = claims.get("userId");
////
////            log.debug("Available claims: {}", claims.keySet());
////            log.debug("userId claim value: {} (type: {})", userIdClaim,
////                    userIdClaim != null ? userIdClaim.getClass().getSimpleName() : "null");
////
////            if (userIdClaim != null) {
////                if (userIdClaim instanceof Number) {
////                    return ((Number) userIdClaim).longValue();
////                } else if (userIdClaim instanceof String) {
////                    return Long.parseLong((String) userIdClaim);
////                }
////            }
////
////            log.error("No valid userId found in token claims");
////            throw new RuntimeException("User ID not found in token");
////
////        } catch (Exception e) {
////            log.error("Error extracting user ID from token: {}", e.getMessage());
////            throw new RuntimeException("Invalid token: " + e.getMessage());
////        }
////    }
////
////    public String getUsernameFromToken(String token) {
////        try {
////            Claims claims = Jwts.parser()
////                    .setSigningKey(key)
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody();
////
////            return claims.getSubject();
////        } catch (Exception e) {
////            log.error("Error extracting username from token: {}", e.getMessage());
////            throw new RuntimeException("Invalid token");
////        }
////    }
////
////    public Authentication getAuthentication(String token) {
////        try {
////            Claims claims = Jwts.parser()
////                    .setSigningKey(key)
////                    .build()
////                    .parseClaimsJws(token)
////                    .getBody();
////
////            String username = claims.getSubject();
////            String role = claims.get("role", String.class);
////
////            Collection<? extends GrantedAuthority> auths;
////            if (role != null && !role.isEmpty()) {
////                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
////                auths = Arrays.asList(new SimpleGrantedAuthority(authority));
////            } else {
////                auths = Arrays.asList(new SimpleGrantedAuthority("ROLE_STUDENT"));
////            }
////
////            log.debug("Created authentication for user: {} with role: {}", username, role);
////            return new UsernamePasswordAuthenticationToken(username, token, auths);
////
////        } catch (Exception e) {
////            log.error("Error creating authentication from token: {}", e.getMessage());
////            throw new RuntimeException("Invalid token");
////        }
////    }
////}
//
//
//package edu.icet.ecom.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.MalformedJwtException;
//import io.jsonwebtoken.UnsupportedJwtException;
//import io.jsonwebtoken.security.Keys;
//import io.jsonwebtoken.security.SecurityException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//import java.util.Collection;
//
//@Component
//@Slf4j
//public class JwtTokenUtil {
//
//    private final SecretKey key;
//
//    public JwtTokenUtil(@Value("${application.security.jwt.secret-key}") String jwtSecret) {
//        // CRITICAL FIX: Ensure proper key generation that matches user service
//        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
//
//        // If the key is hex-encoded (which it appears to be), decode it
//        if (jwtSecret.length() == 64 && jwtSecret.matches("[0-9A-Fa-f]+")) {
//            keyBytes = hexStringToByteArray(jwtSecret);
//        }
//
//        this.key = Keys.hmacShaKeyFor(keyBytes);
//
//        log.info("JWT TokenUtil initialized:");
//        log.info("- Secret key length: {} characters", jwtSecret.length());
//        log.info("- Key bytes length: {} bytes", keyBytes.length);
//        log.info("- Algorithm: {}", this.key.getAlgorithm());
//        log.info("- Secret key preview: {}...",
//                jwtSecret.length() > 10 ? jwtSecret.substring(0, 10) : jwtSecret);
//    }
//
//    private byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            log.debug("Validating JWT token...");
//            log.debug("Token length: {}", token.length());
//            log.debug("Token header: {}", token.substring(0, Math.min(50, token.length())));
//
//            Claims claims = Jwts.parser()
//                    .verifyWith(key)  // Use verifyWith for newer versions
//                    .build()
//                    .parseSignedClaims(token)  // Use parseSignedClaims
//                    .getPayload();
//
//            log.debug("Token validation successful");
//            log.debug("Token claims: {}", claims.keySet());
//            log.debug("Subject: {}", claims.getSubject());
//            log.debug("Issued at: {}", claims.getIssuedAt());
//            log.debug("Expires at: {}", claims.getExpiration());
//
//            return true;
//
//        } catch (ExpiredJwtException e) {
//            log.error("JWT token is expired: {}", e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            log.error("JWT token is unsupported: {}", e.getMessage());
//        } catch (MalformedJwtException e) {
//            log.error("Invalid JWT token format: {}", e.getMessage());
//        } catch (SecurityException e) {
//            log.error("JWT signature validation failed: {}", e.getMessage());
//            log.error("This usually means the secret key doesn't match between services");
//        } catch (IllegalArgumentException e) {
//            log.error("JWT claims string is empty: {}", e.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected JWT validation error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
//        }
//        return false;
//    }
//
//    public Long getUserIdFromToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            // Debug all available claims
//            log.debug("Available claims in token: {}", claims.keySet());
//            claims.forEach((key, value) ->
//                    log.debug("Claim '{}': {} (type: {})", key, value,
//                            value != null ? value.getClass().getSimpleName() : "null"));
//
//            // Try to get userId claim
//            Object userIdClaim = claims.get("userId");
//            if (userIdClaim != null) {
//                if (userIdClaim instanceof Number) {
//                    return ((Number) userIdClaim).longValue();
//                } else if (userIdClaim instanceof String) {
//                    return Long.parseLong((String) userIdClaim);
//                }
//            }
//
//            log.error("No valid userId found in token claims");
//            throw new RuntimeException("User ID not found in token");
//
//        } catch (Exception e) {
//            log.error("Error extracting user ID from token: {}", e.getMessage());
//            throw new RuntimeException("Invalid token: " + e.getMessage());
//        }
//    }
//
//    public String getUsernameFromToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            return claims.getSubject();
//        } catch (Exception e) {
//            log.error("Error extracting username from token: {}", e.getMessage());
//            throw new RuntimeException("Invalid token");
//        }
//    }
//
//    public Authentication getAuthentication(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .verifyWith(key)
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            String username = claims.getSubject();
//            String role = claims.get("role", String.class);
//
//            Collection<? extends GrantedAuthority> auths;
//            if (role != null && !role.isEmpty()) {
//                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
//                auths = Arrays.asList(new SimpleGrantedAuthority(authority));
//            } else {
//                auths = Arrays.asList(new SimpleGrantedAuthority("ROLE_STUDENT"));
//            }
//
//            log.debug("Created authentication for user: {} with role: {}", username, role);
//            return new UsernamePasswordAuthenticationToken(username, token, auths);
//
//        } catch (Exception e) {
//            log.error("Error creating authentication from token: {}", e.getMessage());
//            throw new RuntimeException("Invalid token");
//        }
//    }
//
//    // Debug method to inspect token without validation
//    public void debugTokenStructure(String token) {
//        try {
//            // Split the JWT to examine its parts
//            String[] parts = token.split("\\.");
//            log.info("=== JWT TOKEN STRUCTURE DEBUG ===");
//            log.info("Number of parts: {}", parts.length);
//
//            if (parts.length >= 1) {
//                String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
//                log.info("Header: {}", header);
//            }
//
//            if (parts.length >= 2) {
//                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
//                log.info("Payload: {}", payload);
//            }
//
//            if (parts.length >= 3) {
//                log.info("Signature length: {} characters", parts[2].length());
//            }
//
//            log.info("=== END TOKEN DEBUG ===");
//
//        } catch (Exception e) {
//            log.error("Error debugging token structure: {}", e.getMessage());
//        }
//    }
//}

package edu.icet.ecom.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

@Component
@Slf4j
public class JwtTokenUtil {

    private final SecretKey key;

    public JwtTokenUtil(@Value("${application.security.jwt.secret-key}") String jwtSecret) {
        // CRITICAL FIX: Use identical key generation as user service
        byte[] keyBytes;

        // If the key is hex-encoded (which it is), decode it properly
        if (jwtSecret.length() == 64 && jwtSecret.matches("[0-9A-Fa-f]+")) {
            keyBytes = hexStringToByteArray(jwtSecret);
        } else {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);

        log.info("JWT TokenUtil initialized:");
        log.info("- Secret key length: {} characters", jwtSecret.length());
        log.info("- Key bytes length: {} bytes", keyBytes.length);
        log.info("- Algorithm: {}", this.key.getAlgorithm());
        log.info("- Secret key preview: {}...",
                jwtSecret.length() > 10 ? jwtSecret.substring(0, 10) : jwtSecret);
    }

    // CRITICAL: Add hex decoder method identical to user service
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public boolean validateToken(String token) {
        try {
            log.debug("Validating JWT token...");
            log.debug("Token length: {}", token.length());

            // CRITICAL FIX: Use the same method as user service
            Claims claims = Jwts.parser()
                    .setSigningKey(key)  // Use setSigningKey for compatibility with user service
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.debug("Token validation successful");
            log.debug("Token claims: {}", claims.keySet());
            log.debug("Subject: {}", claims.getSubject());
            log.debug("User ID: {}", claims.get("userId"));
            log.debug("Role: {}", claims.get("role"));

            return true;

        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format: {}", e.getMessage());
        } catch (SecurityException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            log.error("This usually means the secret key doesn't match between services");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected JWT validation error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return false;
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Debug all available claims
            log.debug("Available claims in token: {}", claims.keySet());
            claims.forEach((key, value) ->
                    log.debug("Claim '{}': {} (type: {})", key, value,
                            value != null ? value.getClass().getSimpleName() : "null"));

            // Try to get userId claim
            Object userIdClaim = claims.get("userId");
            if (userIdClaim != null) {
                if (userIdClaim instanceof Number) {
                    return ((Number) userIdClaim).longValue();
                } else if (userIdClaim instanceof String) {
                    return Long.parseLong((String) userIdClaim);
                }
            }

            log.error("No valid userId found in token claims");
            throw new RuntimeException("User ID not found in token");

        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            Collection<? extends GrantedAuthority> auths;
            if (role != null && !role.isEmpty()) {
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                auths = Arrays.asList(new SimpleGrantedAuthority(authority));
            } else {
                auths = Arrays.asList(new SimpleGrantedAuthority("ROLE_STUDENT"));
            }

            log.debug("Created authentication for user: {} with role: {}", username, role);
            return new UsernamePasswordAuthenticationToken(username, token, auths);

        } catch (Exception e) {
            log.error("Error creating authentication from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    // Debug method to inspect token without validation
    public void debugTokenStructure(String token) {
        try {
            // Split the JWT to examine its parts
            String[] parts = token.split("\\.");
            log.info("=== JWT TOKEN STRUCTURE DEBUG ===");
            log.info("Number of parts: {}", parts.length);

            if (parts.length >= 1) {
                String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
                log.info("Header: {}", header);
            }

            if (parts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                log.info("Payload: {}", payload);
            }

            if (parts.length >= 3) {
                log.info("Signature length: {} characters", parts[2].length());
            }

            log.info("=== END TOKEN DEBUG ===");

        } catch (Exception e) {
            log.error("Error debugging token structure: {}", e.getMessage());
        }
    }
}