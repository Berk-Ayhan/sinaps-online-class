package com.sinaps.onlineclass.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sinaps.onlineclass.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);
}
