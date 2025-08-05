package com.charginghive.auth.service;


import com.charginghive.auth.dto.UserAdminDto;
import com.charginghive.auth.dto.UserEdirDto;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.charginghive.auth.dto.UserRegistrationReq;
import com.charginghive.auth.entity.UserRegistration;
import com.charginghive.auth.repository.UserRepository;
import com.charginghive.auth.security.JwtUtils;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final JwtUtils jwtUtils;


	public String saveUserDetails(UserRegistrationReq credential) {
		String mesg = "user regestration failed!";
		try {
			System.out.println("user passowrd is: "+credential.getPassword());
			credential.setPassword(passwordEncoder.encode(credential.getPassword()));	
			
			repository.save(modelMapper.map(credential, UserRegistration.class));
			mesg = "user registred successfully!";
		}
		catch(Exception ex) {
			System.out.println("Error occured during registering user "+ex.getMessage());
		}
		return mesg;
	}

	public String generateToken(Authentication validAuth) {
		return jwtUtils.generateJwtToken(validAuth);
	}

    public List<UserRegistration> getAllUsers() {
		List<UserRegistration> list = new ArrayList<>();
		try{
			list = repository.findAll();
		}
		catch (Exception ex){
			System.out.println(ex.getMessage());
		}

		return list;
    }

	public String editUserDetails(UserEdirDto credential, Long id) {
		String msg = "User update failed!";
		try {
			// Fetch user securely using ID
			UserRegistration user = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

			// Optional: only allow email update if it hasn't changed or is valid
			user.setName(credential.getName());
			user.setEmail(credential.getEmail());
			user.setPassword(passwordEncoder.encode(credential.getPassword()));

			repository.save(user);
			msg = "User updated successfully!";
		} catch (Exception ex) {
			System.out.println("Error updating user: " + ex.getMessage());
		}
		return msg;
	}


	public UserAdminDto getById(Long id) {
		UserRegistration user = repository.findById(id).orElseThrow();
		return modelMapper.map(user, UserAdminDto.class);
	}
}
