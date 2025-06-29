package com.imageProcessing.demo.security;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.imageProcessing.demo.security.jWT.jwtFilter;
import com.imageProcessing.demo.security.usedDetails.userDetailsImplService;

import lombok.AllArgsConstructor;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@AllArgsConstructor
public class securityConfig {

    private userDetailsImplService usd;
    private jwtFilter jwtFilter;

    @Bean
    public BCryptPasswordEncoder encodePassword(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authmgr(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authprovider(){
        DaoAuthenticationProvider DaoAuth=new DaoAuthenticationProvider();
        DaoAuth.setUserDetailsService(usd);
        DaoAuth.setPasswordEncoder(encodePassword());
        return DaoAuth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth->auth
        .requestMatchers("/auth").authenticated()
        .anyRequest().permitAll());
        http.authenticationProvider(authprovider());
        http.addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);
        return http.build();
        
    }
}
