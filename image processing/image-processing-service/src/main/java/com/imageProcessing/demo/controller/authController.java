package com.imageProcessing.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.imageProcessing.demo.Dtos.authDTO;
import com.imageProcessing.demo.service.authService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class authController {

    private authService aserv;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody authDTO aDto){
        return ResponseEntity.ok("Registered Successfully\n "+ "jwt token "+aserv.registerUser(aDto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody authDTO aDto){
        return ResponseEntity.ok("Jwt token: "+aserv.loginUser(aDto));
    }

    @PostMapping("/updatepassword")
        public ResponseEntity<String> updatePass(@RequestBody authDTO aDto, @RequestParam String newPassword ){
            aserv.updatePassword(aDto.getUsername(), newPassword);
            return ResponseEntity.ok("Password updated successfully");
        }
}

