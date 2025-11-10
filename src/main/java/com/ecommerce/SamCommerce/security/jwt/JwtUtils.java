package com.ecommerce.SamCommerce.security.jwt;

import com.ecommerce.SamCommerce.security.services.UserDetailsImplementation;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    //CORE UTILITY CLASS FOR CREATING PARSING AND VALIDATING JWTS...

    @Value("${spring.app.jwtExpirationMs}")
    private int JwtExpirationTimeMs; //VALIDITY PERIOD OF THE TOKEN
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret; //SECRET KEY TO VERIFY THE JWT

    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie; //THIS IS FROM APPLICATION PROPERTIES FILE

    //GETTING THE LOGS
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    //WHEN WE ARE USING COOKIES WE DON'T NEED THIS METHOD
    //GETTING JWT FROM THE REQUEST
//    public String getJwtFromHeader(HttpServletRequest request){
//        String bearerToken = request.getHeader("Authorization"); //READS THE AUTHORIZATION HEADER
//        logger.debug("Authorization header: {}", bearerToken);
//        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
//            return bearerToken.substring(7); // REMOVE BEARER AND RETURNS THE TOKEN
//        }
//        return null;
//    }

    //GETTING THE TOKEN FROM THE COOKIES
    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, jwtCookie); //IT WILL GIVE US THE COOKIE FROM THE REQUEST
        if(cookie != null){
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImplementation userPrincipal){
        String jwt = generateTokenFromUserName(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(false)
                .build();
        return cookie;
    }

    public ResponseCookie getCleanJwtCookie(){

        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
                .path("/api")
                .build();
        return cookie;
    }

    //GENERATING TOKEN FROM USERNAME
    public String generateTokenFromUserName(String userName){
        return Jwts.builder()
                .subject(userName) //ADD THE USERNAME AS THE SUBJECT CLAIM
                .issuedAt(new Date()) //SETS THE CURRENT TIME
                .expiration(new Date(new Date().getTime() + JwtExpirationTimeMs)) //SETS EXPIRY
                .signWith(key()) //SIGN TOKEN WITH SECRET KEY
                .compact(); //FINALIZE AND RETURN THE STRING JWT

    }

    //GETTING USERNAME FROM JWT TOKEN
    public String getUserNameFromJwtToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())    //SECRET KEY TO VALIDATE
                .build()
                .parseSignedClaims(token) //PASS THE JWT AND EXTRACT THE CLAIMS
                .getPayload()
                .getSubject();
    }

    //GENERATE SIGNING KEY
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    //VALIDATE JWT TOKEN
    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("Validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        }catch (MalformedJwtException exception){
            logger.error("Invalid JWT token: {}", exception.getMessage());
        }catch (ExpiredJwtException exception){
            logger.error("Expired JWT token: {}", exception.getMessage());
        }catch (UnsupportedJwtException exception){
            logger.error("Unsupported JWT token: {}", exception.getMessage());
        }catch (IllegalArgumentException exception){
            logger.error("Empty String JWT token: {}", exception.getMessage());
        }
        return false;
    }

}
