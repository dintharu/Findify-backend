////package edu.icet.ecom.security;
////
////import io.jsonwebtoken.Claims;
////import io.jsonwebtoken.Jwts;
////import io.jsonwebtoken.SignatureAlgorithm;
////import io.jsonwebtoken.io.Decoders;
////import io.jsonwebtoken.security.Keys;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.security.core.userdetails.UserDetails;
////import org.springframework.stereotype.Service;
////
////import java.security.Key;
////import java.util.Date;
////import java.util.HashMap;
////import java.util.Map;
////import java.util.function.Function;
////
////@Service
////public class JwtService {
////
////    @Value("${application.security.jwt.secret-key}")
////    private String secretKey;
////
////    @Value("${application.security.jwt.expiration}")
////    private long jwtExpiration;
////
////    public String extractUsername(String token) {
////        return extractClaim(token, Claims::getSubject);
////    }
////
////    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
////        final Claims claims = extractAllClaims(token);
////        return claimsResolver.apply(claims);
////    }
////
////    public String generateToken(UserDetails userDetails) {
////        return generateToken(new HashMap<>(), userDetails);
////    }
////
////    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
////        return buildToken(extraClaims, userDetails, jwtExpiration);
////    }
////
////    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
////        return Jwts
////                .builder()
////                .setClaims(extraClaims)
////                .setSubject(userDetails.getUsername())
////                .setIssuedAt(new Date(System.currentTimeMillis()))
////                .setExpiration(new Date(System.currentTimeMillis() + expiration))
////                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
////                .compact();
////    }
////
////    public boolean isTokenValid(String token, UserDetails userDetails) {
////        final String username = extractUsername(token);
////        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
////    }
////
////    private boolean isTokenExpired(String token) {
////        return extractExpiration(token).before(new Date());
////    }
////
////    private Date extractExpiration(String token) {
////        return extractClaim(token, Claims::getExpiration);
////    }
////
////    private Claims extractAllClaims(String token) {
////        return Jwts
////                .parser()
////                .setSigningKey(getSignInKey())
////                .build()
////                .parseClaimsJws(token)
////                .getBody();
////    }
////
////    private Key getSignInKey() {
////        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
////        return Keys.hmacShaKeyFor(keyBytes);
////    }
////}
//
////
////package edu.icet.ecom.security;
////
////import io.jsonwebtoken.Claims;
////import io.jsonwebtoken.Jwts;
////import io.jsonwebtoken.SignatureAlgorithm;
////import io.jsonwebtoken.io.Decoders;
////import io.jsonwebtoken.security.Keys;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.security.core.userdetails.UserDetails;
////import org.springframework.stereotype.Service;
////
////import java.security.Key;
////import java.util.Date;
////import java.util.HashMap;
////import java.util.Map;
////import java.util.function.Function;
////
////@Service
////public class JwtService {
////
////    @Value("${application.security.jwt.secret-key}")
////    private String secretKey;
////
////    @Value("${application.security.jwt.expiration}")
////    private long jwtExpiration;
////
////    public String extractUsername(String token) {
////        return extractClaim(token, Claims::getSubject);
////    }
////
////    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
////        final Claims claims = extractAllClaims(token);
////        return claimsResolver.apply(claims);
////    }
////
////    // FIXED: Enhanced token generation with user ID
////    public String generateToken(UserDetails userDetails) {
////        Map<String, Object> extraClaims = new HashMap<>();
////
////        // CRITICAL: Add user ID to token claims for chat service
////        if (userDetails instanceof edu.icet.ecom.model.entity.User) {
////            edu.icet.ecom.model.entity.User user = (edu.icet.ecom.model.entity.User) userDetails;
////            extraClaims.put("userId", user.getId());
////            extraClaims.put("fullName", user.getFullname());
////            extraClaims.put("role", user.getRole().name());
////        }
////
////        return generateToken(extraClaims, userDetails);
////    }
////
////    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
////        return buildToken(extraClaims, userDetails, jwtExpiration);
////    }
////
////    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
////        return Jwts
////                .builder()
////                .setClaims(extraClaims)
////                .setSubject(userDetails.getUsername())
////                .setIssuedAt(new Date(System.currentTimeMillis()))
////                .setExpiration(new Date(System.currentTimeMillis() + expiration))
////                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
////                .compact();
////    }
////
////    public boolean isTokenValid(String token, UserDetails userDetails) {
////        final String username = extractUsername(token);
////        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
////    }
////
////    private boolean isTokenExpired(String token) {
////        return extractExpiration(token).before(new Date());
////    }
////
////    private Date extractExpiration(String token) {
////        return extractClaim(token, Claims::getExpiration);
////    }
////
////    private Claims extractAllClaims(String token) {
////        return Jwts
////                .parser()
////                .setSigningKey(getSignInKey())
////                .build()
////                .parseClaimsJws(token)
////                .getBody();
////    }
////
////    private Key getSignInKey() {
////        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
////        return Keys.hmacShaKeyFor(keyBytes);
////    }
////}
//
//package edu.icet.ecom.security;
//
//import edu.icet.ecom.model.entity.User;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import java.security.Key;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//@Slf4j
//public class JwtService {
//
//    @Value("${application.security.jwt.secret-key}")
//    private String secretKey;
//
//    @Value("${application.security.jwt.expiration}")
//    private long jwtExpiration;
//
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // CRITICAL FIX: Enhanced token generation with proper user ID handling
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> extraClaims = new HashMap<>();
//
//        // CRITICAL: Add user ID to token claims for chat service
//        if (userDetails instanceof User) {
//            User user = (User) userDetails;
//            extraClaims.put("userId", user.getId());
//            extraClaims.put("fullName", user.getFullname());
//            extraClaims.put("role", user.getRole().name());
//
//            log.debug("Generating token for user: ID={}, Email={}, Role={}",
//                    user.getId(), user.getEmail(), user.getRole());
//        } else {
//            log.warn("UserDetails is not an instance of User: {}", userDetails.getClass());
//        }
//
//        return generateToken(extraClaims, userDetails);
//    }
//
//    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//        return buildToken(extraClaims, userDetails, jwtExpiration);
//    }
//
//    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
//        String token = Jwts
//                .builder()
//                .setClaims(extraClaims)
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
//                .compact();
//
//        log.debug("JWT token generated successfully for user: {}", userDetails.getUsername());
//        return token;
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    private Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    private Claims extractAllClaims(String token) {
//        return Jwts
//                .parser()
//                .setSigningKey(getSignInKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    private Key getSignInKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//}

package edu.icet.ecom.security;

import edu.icet.ecom.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // CRITICAL FIX: Enhanced token generation with proper user ID handling
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        // CRITICAL: Add user ID to token claims for chat service
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            extraClaims.put("userId", user.getId());
            extraClaims.put("fullName", user.getFullname());
            extraClaims.put("role", user.getRole().name());

            log.debug("Generating token for user: ID={}, Email={}, Role={}",
                    user.getId(), user.getEmail(), user.getRole());
        } else {
            log.warn("UserDetails is not an instance of User: {}", userDetails.getClass());
        }

        return generateToken(extraClaims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        String token = Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        log.debug("JWT token generated successfully for user: {}", userDetails.getUsername());
        log.debug("Token length: {}", token.length());
        log.debug("Secret key used (first 10 chars): {}", secretKey.substring(0, Math.min(10, secretKey.length())));

        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // CRITICAL FIX: Ensure identical key generation as chat service
    private Key getSignInKey() {
        // CRITICAL: Use the exact same key generation method as chat service
        byte[] keyBytes;

        // If the key is hex-encoded (which it is), decode it properly
        if (secretKey.length() == 64 && secretKey.matches("[0-9A-Fa-f]+")) {
            keyBytes = hexStringToByteArray(secretKey);
        } else {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // CRITICAL: Add hex decoder method to match chat service
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}