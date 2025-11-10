package com.ecommerce.SamCommerce.security.jwt;

import com.ecommerce.SamCommerce.security.services.UserDetailsServiceImplementation;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {
    //CREATING CUSTOM FILTER USING THIS AUTHTOKENFILTER CLASS
    //PURPOSE -> is to intercept incoming HTTP requests, check if there is a valid JWT token, and if yes, set the authentication in Spring Security’s SecurityContext.
    //OncePerRequestFilter -> ENSURES FILTER IS EXECUTED ONLY ONCE PER REQUEST

    @Autowired
    private JwtUtils jwtUtils; //FOR PARSING AND VALIDATING THE JWTs
    @Autowired
    private UserDetailsServiceImplementation userDetailsServiceImplementation; //TO LOAD THE USER DETAILS

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    //THIS METHOD RUNS FOR EVERY INCOMING REQUEST BEFORE THE CONTROLLER IS CALLED
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        // ✅ Skip JWT validation for authentication and public endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("AuthTokenFilter called for URI: {}", path);
        try{
            String jwt = parseJwt(request); //JWT TOKEN FROM THE REQUEST HEADER
            if(jwt!=null && jwtUtils.validateJwtToken(jwt)){
                String userName = jwtUtils.getUserNameFromJwtToken(jwt); //EXTRACT THE USERNAME EMBEDDED IN THE TOKEN
                UserDetails userDetails = userDetailsServiceImplementation.loadUserByUsername(userName); //FETCHING USER DETAILS
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());  //CREATING SPRING SECURITY OBJECT WITH USER DETAILS AND ROLES
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //ADDS REQUEST SPECIFIC DETAILS -> IP,SESSION
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                //STORE THE AUTHENTICATION -> SPRING SECURITY NOW KNOWS THE USER IS AUTHENTICATED
                //CONTROLLER CAN ACCESS THE USER VIA @AUTHENTICATIONPRINCIPAL OR SECURITYCONTEXTHOLDER
                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());
            }
        }catch (Exception e){
                logger.error("Cannot set user authentication: {}", e);
        }
        filterChain.doFilter(request,response); //PASSES THE REQUEST ALONG THE FILTER CHAIN

    }

    //EXTRACTING JWT from the HTTP header (typically Authorization: Bearer <token>).
    private String parseJwt(HttpServletRequest request) {
        String jwtHeader = jwtUtils.getJwtFromCookies(request); //GETTING THE AUTHORIZATION TOKEN HEADER
        logger.debug("AuthTokenFilter.java : {}", jwtHeader);
        return  jwtHeader;
    }
}
