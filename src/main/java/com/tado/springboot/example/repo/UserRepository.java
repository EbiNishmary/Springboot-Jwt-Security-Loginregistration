package com.tado.springboot.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tado.springboot.example.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    void deleteById(Long id);

    Optional<User> findById (Long id);
    
	
	User findByUsername(String username);

	User findByEmail(String email);
}
