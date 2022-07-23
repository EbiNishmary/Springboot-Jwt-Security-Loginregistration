package com.tado.springboot.example.service;

import com.tado.springboot.example.model.User;
import com.tado.springboot.example.model.UserDto;
import com.tado.springboot.example.repo.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with the email of: " + email);
        }

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                new ArrayList<>());
    }

    public User save (User user) {
        User newUser = new User(user.getUsername(), bcryptEncoder.encode(user.getPassword()), user.getEmail()
               );

        return repo.save(newUser);
    }
}
