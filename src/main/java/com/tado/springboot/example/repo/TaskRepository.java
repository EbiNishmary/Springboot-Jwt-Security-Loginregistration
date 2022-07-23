package com.tado.springboot.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tado.springboot.example.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
