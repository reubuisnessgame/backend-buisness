package com.github.senyast4745.gamebank.dao;

import com.github.senyast4745.gamebank.model.UserModel;
import com.github.senyast4745.gamebank.repository.UserRepository;
import com.github.senyast4745.gamebank.secutity.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UtilBean {

    private final UserRepository userRepository;

    private final
    JwtTokenProvider jwtTokenProvider;

    public UtilBean(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    UserModel getUserByToken(String token) {
        Long userId = getUserIdFromToken(token);
        return userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User ID: " + userId + " not found"));
    }


    private Long getUserIdFromToken(String token) {
        Jws<Claims> claims = jwtTokenProvider.getClaims(resolveToken(token));
        return (Long) claims.getBody().get("userId");
    }

    private String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Incorrect token");
    }

    UserModel getTeam(Long number) {
        return userRepository.findByTeamNumber(number).orElseThrow(() ->
                new UsernameNotFoundException("Number: " + number + " not found"));
    }

    Long calculateFullScore(Long number){

    }

}
