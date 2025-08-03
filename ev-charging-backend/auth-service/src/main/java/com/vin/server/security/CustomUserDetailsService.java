package com.vin.server.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.vin.server.entity.UserRegistration;
import com.vin.server.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository repository;
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserRegistration user = repository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Invalid Email!!!"));
		return user;
	}

}
