package com.ecommerce.SamCommerce.Controllers;

import com.ecommerce.SamCommerce.Entity.AppRole;
import com.ecommerce.SamCommerce.Entity.Role;
import com.ecommerce.SamCommerce.Entity.User;
import com.ecommerce.SamCommerce.Repositories.RoleRepository;
import com.ecommerce.SamCommerce.Repositories.UserRepository;
import com.ecommerce.SamCommerce.security.jwt.JwtUtils;
import com.ecommerce.SamCommerce.security.request.LoginRequest;
import com.ecommerce.SamCommerce.security.request.SignupRequest;
import com.ecommerce.SamCommerce.security.response.MessageResponse;
import com.ecommerce.SamCommerce.security.response.UserInfoResponse;
import com.ecommerce.SamCommerce.security.services.UserDetailsImplementation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate( //TO AUTHENTICATE THE USER
                    new UsernamePasswordAuthenticationToken( //IMPLEMENTATION OF AUTHENTICATION INTERFACE
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        }catch (AuthenticationException exception){
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad Credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
        }
        //STORING THE AUTHENTICATION OBJECT IN SECURITY CONTEXT FOR SPRING SECURITY
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //GENERATE THE TOKEN
        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        UserInfoResponse response = new UserInfoResponse(userDetails.getId(),roles, userDetails.getUsername(), jwtCookie.toString());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> resisterUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userRepository.existsByUserName(signupRequest.getUsername())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error! Username is already taken."));
        }

        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Error! Email is already taken."));
        }

        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        //USER SENDING THE ROLE
        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();
        if(strRoles == null){
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(()-> new RuntimeException("Role is not found!"));
            roles.add(userRole);
        }else{
            strRoles.forEach(role -> {
                switch (role){
                    case "admin":
                        Role userRoleAdmin = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(()-> new RuntimeException("Role is not found!"));
                        roles.add(userRoleAdmin);
                        break;
                    case "seller":
                        Role userRoleSeller = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(()-> new RuntimeException("Role is not found!"));
                        roles.add(userRoleSeller);
                        break;
                    default:
                        Role userRoleDefault = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(()-> new RuntimeException("Role is not found!"));
                        roles.add(userRoleDefault);
                }
            });
        }

        // 3. CRITICAL CHECKPOINT: Log the email right before save
        //System.out.println("Email being saved to DB: [" + user.getEmail() + "]");
        // If this output is NOT "usertestemail@gmail.com", your mapping is broken
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User Registration is successful"));
    }

    //THIS METHOD WILL DISPLAY USERNAME AS A STRING
    @GetMapping("/username")
    public String currentUserName(Authentication authentication){
        if(authentication!=null){
            return authentication.getName();
        }
        return "";

    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication){
        UserDetailsImplementation userDetailsImplementation = (UserDetailsImplementation) authentication.getPrincipal();
        List<String> roles = userDetailsImplementation.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        UserInfoResponse response = new UserInfoResponse(userDetailsImplementation.getId(),roles, userDetailsImplementation.getUsername());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(new MessageResponse("You Signed out!"));

    }
}
