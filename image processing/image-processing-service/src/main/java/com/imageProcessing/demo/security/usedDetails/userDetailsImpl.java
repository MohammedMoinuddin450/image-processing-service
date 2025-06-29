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
public class userDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private int userid;
    private String username;
    private String password;
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public static UserDetails build(User user) {
        return new userDetailsImpl(
            user.getUserid(),
            user.getUserName(),
            user.getPassword(),
            user
        );
    }
}

