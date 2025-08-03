package com.vin.server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vin.server.entity.UserRegistration;
import java.util.List;


public interface UserRepository extends JpaRepository<UserRegistration, Long> {

	Optional<UserRegistration> findByEmail(String email);

}
