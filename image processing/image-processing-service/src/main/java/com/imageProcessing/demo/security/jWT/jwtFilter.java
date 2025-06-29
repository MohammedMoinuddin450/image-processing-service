package com.imageProcessing.demo.security.jWT;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.imageProcessing.demo.security.usedDetails.userDetailsImplService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class jwtFilter extends OncePerRequestFilter {
   
    @Autowired
    private jwtUtils jwtprovider;
    @Autowired
    private userDetailsImplService usis;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
       try {
        String jwt=jwtprovider.getJwtHeader(request);
        if(jwt!=null && jwtprovider.validateJwt(jwt)){
            String userName=jwtprovider.usernameFromPayload(jwt);
            UserDetails uDetails=usis.loadUserByUsername(userName);
            if(uDetails!=null){
                UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(uDetails,null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
       } catch (Exception e) {
        e.printStackTrace();
       }

       filterChain.doFilter(request, response);
   }

}
