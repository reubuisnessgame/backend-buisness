package com.github.senyast4745.gamebank.controller;


import com.github.senyast4745.gamebank.form.AuthForm;
import com.github.senyast4745.gamebank.model.UserModel;
import com.github.senyast4745.gamebank.repository.UserRepository;
import com.github.senyast4745.gamebank.secutity.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final
    JwtTokenProvider jwtTokenProvider;

    private final
    UserRepository userRepository;

    @Autowired
    public AuthController(JwtTokenProvider jwtTokenProvider, UserRepository userRepository, AuthenticationManager authenticationManager) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    @RequestMapping(value = "/{teamNumber}",method = RequestMethod.POST)
        public ResponseEntity signIn(@PathVariable Long teamNumber) {
        try {
            UserModel userModel = userRepository.findByTeamNumber(teamNumber)
                    .orElse(userRepository.save(new UserModel(teamNumber, 0D, 0D, 0D, 0L,0L)));
            //TODO thinking about authenticationManager
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(teamNumber, null));
            String token = jwtTokenProvider.createToken(teamNumber.toString(), userRepository.findByUsername(teamNumber.toString()).orElseThrow(()
                    -> new UsernameNotFoundException("Username " + teamNumber + "not found")).getRole().name());

            Map<Object, Object> model = new HashMap<>();
            model.put("model", userModel);
            model.put("token", token);
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password supplied or account locked");
        }
    }

    @RequestMapping(value = "/admins", method = RequestMethod.POST)
    public ResponseEntity signInAdmin(@RequestBody AuthForm authForm) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authForm.getUsername(), authForm.getPassword()));
            String token = jwtTokenProvider.createToken(authForm.getUsername(), this.userRepository.findByUsername(authForm.getUsername()).orElseThrow(()
                    -> new UsernameNotFoundException("Username " + authForm.getUsername() + "not found")).getRole().name());

            Map<Object, Object> model = new HashMap<>();
            model.put("username", authForm.getUsername());
            model.put("token", token);
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password supplied or account locked");
        }
    }


}
