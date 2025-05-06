package com.imageProcessing.demo.security.usedDetails;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.imageProcessing.demo.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class userDetailsImpl implements UserDetails{

    private int userid;
    private String userName;
    private String password;



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;    
    }

    @Override
    public String getUsername() {
        return userName;
    }

    public static userDetailsImpl build(User user){
        return new userDetailsImpl(user.getUserid(),
        user.getPassword(),
        user.getUserName());
    }

}
