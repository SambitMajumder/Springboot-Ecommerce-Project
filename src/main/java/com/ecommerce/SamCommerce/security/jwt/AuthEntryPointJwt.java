package com.ecommerce.SamCommerce.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//RETURN A CUSTOM RESPONSE -> IT WILL GIVE THE USER A CLEAR JSON ERROR RESPONSE WHEN THERE IS A USER AUTHENTICATION FAILURE
//SUMMARY -> THIS CLASS IS TO HANDLE AUTHENTICATION FAILURE

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        logger.error("Unauthorized error: {}", authException.getMessage());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //SET THE TYPE - JSON RESPONSE
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //SET THE STATUS - HTTP 401
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", authException.getMessage());
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper(); //COVERT THE MAP INTO JSON
        mapper.writeValue(response.getOutputStream(), body); //WRITES JSON DIRECTLY TO THE RESPONSE OUTPUT STREAM
    }
}
