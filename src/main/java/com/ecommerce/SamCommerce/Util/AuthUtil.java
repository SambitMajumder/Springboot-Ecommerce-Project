package com.ecommerce.SamCommerce.Util;

import com.ecommerce.SamCommerce.Entity.User;
import com.ecommerce.SamCommerce.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    private UserRepository userRepository;

    public String loggedInEmail(){
        //AUTHENTICATION WILL HAVE THE DETAILS OF THE AUTHENTICATED USER
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //USING USER REPOSITORY TO GET THE USER DETAILS
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(()->
                new UsernameNotFoundException("User not found: -> " + authentication.getName()));
        return user.getEmail();
    }

    public Integer loggedInUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(()->
                new UsernameNotFoundException("User not found: -> " + authentication.getName()));
        return user.getUserID();
    }

    public User loggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(()->
                new UsernameNotFoundException("User not found: -> " + authentication.getName()));
        return user;
    }
}
