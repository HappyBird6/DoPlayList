package play.dpl.playlist.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    String SECRET;

    public static final int JWT_EXPIRY_TIME = 1000 * 60 * 60; // JWT 토큰 만료 시간 100분
    
    public String generateToken(String email, String userPassword) { 
        System.out.println("generateToken");
        Map<String, Object> claims = new HashMap<>(); 
        claims.put("password", userPassword);
        return createToken(claims, email); 
    } 

    private String createToken(Map<String, Object> claims, String email) { 
        System.out.println("createToken");
        return Jwts.builder() 
                .setClaims(claims) 
                .setSubject(email) 
                .setIssuedAt(new Date(System.currentTimeMillis())) 
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRY_TIME)) 
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact(); 
    } 
    private Key getSignKey() { 
        byte[] keyBytes= Decoders.BASE64.decode(SECRET); 
        return Keys.hmacShaKeyFor(keyBytes); 
    } 

    public String extractUsername(String token) { 
        return extractClaim(token, Claims::getSubject); 
    } 
    public String extractPassword(String token) { 
        Claims claims = extractAllClaims(token);
        return claims.get("password", String.class);
    } 

    public Date extractExpiration(String token) { 
        return extractClaim(token, Claims::getExpiration); 
    } 

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) { 
        final Claims claims = extractAllClaims(token); 
        return claimsResolver.apply(claims); 
    } 
  
    private Claims extractAllClaims(String token) { 
        return Jwts 
                .parserBuilder() 
                .setSigningKey(getSignKey()) 
                .build() 
                .parseClaimsJws(token) 
                .getBody(); 
    } 
  
    private Boolean isTokenExpired(String token) { 
        return extractExpiration(token).before(new Date()); 
    } 
  
    public Boolean validateToken(String token, UserDetails userDetails) { 
        final String username = extractUsername(token); 
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)); 
    } 
    
}