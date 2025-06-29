package com.imageProcessing.demo.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.imageProcessing.demo.Dtos.authDTO;
import com.imageProcessing.demo.model.User;
import com.imageProcessing.demo.repos.userRepo;
import com.imageProcessing.demo.security.jWT.jwtResponse;
import com.imageProcessing.demo.security.jWT.jwtUtils;
import com.imageProcessing.demo.security.usedDetails.userDetailsImpl;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor 
public class authService {

    private PasswordEncoder passwordEncoder;
    private userRepo urepo;
    private jwtUtils utils;
    private AuthenticationManager authenticationManager;

    public jwtResponse registerUser(authDTO aDto) {
       User user=new User();
       user.setUserName(aDto.getUsername());
       user.setPassword(passwordEncoder.encode(aDto.getPassword()));
       urepo.save(user);
        String token = utils.generateToken(userDetailsImpl.build(user));

        return new jwtResponse(token);
    }


    public jwtResponse loginUser(authDTO aDto) {
        try{
            Authentication authentication=authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(aDto.getUsername(), aDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            userDetailsImpl userDetail=(userDetailsImpl)authentication.getPrincipal();
        return new jwtResponse(utils.generateToken(userDetail));
    }catch(AuthenticationException e)
    {
        throw new IllegalArgumentException("Invalid username or password");
    }
    }

    public void updatePassword(String username, String newPassword) {
        User user = urepo.findByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (newPassword == null) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        urepo.save(user);

    }
}