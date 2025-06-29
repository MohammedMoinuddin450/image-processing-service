package com.imageProcessing.demo.security.usedDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.imageProcessing.demo.model.User;
import com.imageProcessing.demo.repos.userRepo;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class userDetailsImplService implements UserDetailsService{
    @Autowired
    private userRepo uRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user=uRepo.findByUserName(username)
        .orElseThrow(()->new UsernameNotFoundException("User with " + username + " not found"));

        return userDetailsImpl.build(user);
    }
}
