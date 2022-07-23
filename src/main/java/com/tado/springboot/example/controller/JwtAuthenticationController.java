package com.tado.springboot.example.controller;

import com.tado.springboot.example.config.JwtUtil;
import com.tado.springboot.example.model.User;
import com.tado.springboot.example.model.UserDto;
import com.tado.springboot.example.repo.UserRepository;
import com.tado.springboot.example.service.JwtUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
    @Autowired
    private UserRepository repo;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @CrossOrigin
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ResponseEntity<User> getCurrentUser (HttpServletRequest request)  {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = repo.findByUsername(((UserDetails) principal).getUsername());
        return ResponseEntity.ok(user);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> registerUser (@RequestBody Map<String, Object> user) throws Exception {
        User savedUser = new User();
        User newUser = new User(
                (String) user.get("username"),
                
                (String) user.get("email"),
                (String) user.get("password")
               
        );

        if (newUser.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is missing.");
        }

        if (newUser.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is missing.");
        }

        if (newUser.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is missing.");
        } else if (newUser.getPassword().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password length must be 8+.");
        }

        

        try {
            savedUser = jwtUserDetailsService.save(newUser);
        } catch (DataIntegrityViolationException e) {
            if (e.getRootCause().getMessage().contains(newUser.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is not available.");
            }

            if (e.getRootCause().getMessage().contains(newUser.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is not available.");
            }
        }

        Map<String, Object> tokenResponse = new HashMap<>();

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(savedUser.getEmail());
        final String token = jwtUtil.generateToken(userDetails);

        tokenResponse.put("token", token);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<?> authenticateUser (@RequestBody Map<String, String> user) throws Exception {
        authenticate(user.get("email"), user.get("password"));

        Map<String, Object> tokenResponse = new HashMap<>();
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.get("email"));
        final String token = jwtUtil.generateToken(userDetails);

        tokenResponse.put("token", token);
        return ResponseEntity.ok(tokenResponse);
    }

    private void authenticate(String email, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new Exception("User disabled", e);
        } catch (BadCredentialsException e) {
            throw new Exception("Invalid credentials", e);
        }
    }
}
