package com.charginghive.auth.controller;

import com.charginghive.auth.dto.UserEdirDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.charginghive.auth.dto.UserRegistrationReq;
import com.charginghive.auth.dto.UserSignInReq;
import com.charginghive.auth.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

	private final UserService service;
	private final AuthenticationManager authenticationManager;
	
	@PostMapping("/register")
	public ResponseEntity<?> addNewUser(@RequestBody UserRegistrationReq credential){
		System.out.println(credential.toString());
		return ResponseEntity.status(HttpStatus.CREATED).body(service.saveUserDetails(credential));
	}
	
	@PostMapping("/login")
	public ResponseEntity<?> authenticateAndGetTocken(@RequestBody UserSignInReq signInReq){
		System.out.println("in Signin "+ signInReq);
		Authentication authToken = new UsernamePasswordAuthenticationToken(signInReq.getEmail(), signInReq.getPassword());
		System.out.println("before authentication- "+authToken.isAuthenticated());
		
		Authentication validAuth = authenticationManager.authenticate(authToken);
		System.out.println("After authentication- "+authToken.isAuthenticated());
		System.out.println(validAuth);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(service.generateToken(validAuth));
	}

	@GetMapping("/get-all")
	public ResponseEntity<?> getAllUsers(){
		return ResponseEntity.status(HttpStatus.OK).body(service.getAllUsers());
	}

	@PutMapping("/edit-user")
	public ResponseEntity<?> editUser(@RequestBody UserEdirDto credential, @RequestHeader("X-User-Id") String userId){
		Long id = Long.parseLong(userId);
		log.info("update user details: "+credential);
		return ResponseEntity.status(HttpStatus.CREATED).body(service.editUserDetails(credential, id));
	}

	//get user by id!
	@GetMapping("/get-by-id/{id}")
	public ResponseEntity<?> getUserById(@PathVariable("id") Long id){
		return ResponseEntity.status(HttpStatus.OK).body(service.getById(id));
	}
}
