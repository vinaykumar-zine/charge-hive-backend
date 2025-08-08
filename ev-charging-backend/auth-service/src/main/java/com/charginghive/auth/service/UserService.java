// The service layer in Spring Boot responsible for handling user-related operations such as registration, token generation, user management, etc.
package com.charginghive.auth.service;

// Importing necessary DTOs (Data Transfer Objects) used to transfer user-related data between layers.
import com.charginghive.auth.dto.UserDto;
import com.charginghive.auth.dto.UserEdirDto;
import com.charginghive.auth.dto.UserResDto;
import com.charginghive.auth.entity.UserRole;

// Utility classes and Spring Framework dependencies
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Entity and repository related to User
import com.charginghive.auth.dto.UserRegistrationReq;
import com.charginghive.auth.entity.UserRegistration;
import com.charginghive.auth.repository.UserRepository;
import com.charginghive.auth.security.JwtUtils;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring Service component
@Transactional // Ensures all public methods in this class are transactional by default
@AllArgsConstructor // Lombok annotation to generate constructor with all final fields
public class UserService {

	private final UserRepository repository; // JPA repository for UserRegistration entity
	private final PasswordEncoder passwordEncoder; // Used to encode passwords securely
	private final ModelMapper modelMapper; // Used to convert between DTOs and entities
	private final JwtUtils jwtUtils; // Custom utility class to generate JWT tokens

	/**
	 * Registers a new user in the system.
	 * @param credential UserRegistrationReq DTO containing user signup details.
	 * @return Success or failure message based on outcome.
	 */
	public String saveUserDetails(UserRegistrationReq credential) {
		String mesg = "user regestration failed!";
		try {
			// Logging raw password for debug purposes (not recommended in production)
			// System.out.println("user passowrd is: " + credential.getPassword());

			// Encode the password before storing it in DB
			credential.setPassword(passwordEncoder.encode(credential.getPassword()));
			Optional<UserRegistration> user = repository.findByEmail(credential.getEmail());
			// Convert DTO to Entity and save to database
//			if(user.isPresent()) {   //it is prone to race condition!
//				return mesg;
//			}
			repository.save(modelMapper.map(credential, UserRegistration.class));

			mesg = "user registred successfully!";
		} catch (Exception ex) {
			// Exception handling with logging
			System.out.println("Error occured during registering user " + ex.getMessage());
		}
		return mesg;
	}

	/**
	 * Generates a JWT token for authenticated user.
	 * @param validAuth Spring Security authentication object.
	 * @return JWT token string.
	 */
	public String generateToken(Authentication validAuth) {
		return jwtUtils.generateJwtToken(validAuth);
	}

	/**
	 * Fetches all non-admin users from the database.
	 * @return List of UserResDto objects.
	 */
	public List<UserResDto> getAllUsers() {
		List<UserResDto> list = new ArrayList<>();
		try {
			// Fetch all users and filter out ADMIN roles before mapping to DTO
			list = repository.findAll().stream()
					.filter(p -> p.getUserRole() != UserRole.ROLE_ADMIN)
					.map(this::mapToDto)
//					.map(p -> modelMapper.map(p, UserResDto.class))
					.collect(Collectors.toList());
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return list;
	}

	/**
	 * Updates existing user's details by their ID.
	 * @param credential UserEdirDto DTO containing updated fields.
	 * @param id User ID to be updated.
	 * @return Success or failure message.
	 */
	public String editUserDetails(UserEdirDto credential, Long id) {
		String msg = "User update failed!";
		try {
			// Fetch the user securely using their ID or throw exception if not found
			UserRegistration user = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

			// Update name, email and encoded password
			user.setName(credential.getName());
			user.setEmail(credential.getEmail());
			user.setPassword(passwordEncoder.encode(credential.getPassword()));

			// Save updated user back to DB
			repository.save(user);
			msg = "User updated successfully!";
		} catch (Exception ex) {
			System.out.println("Error updating user: " + ex.getMessage());
		}
		return msg;
	}

	/**
	 * Returns a specific user by their ID for admin view.
	 * @param id The ID of the user to retrieve.
	 * @return UserAdminDto object with complete user details.
	 */
	public UserDto getById(Long id) {
		UserRegistration user = repository.findById(id).orElseThrow(); // Throws NoSuchElementException if not found
		return modelMapper.map(user, UserDto.class);
	}

	public UserResDto mapToDto(UserRegistration user) {
		return UserResDto.builder()
				.email(user.getEmail())
				.id(user.getId())
				.name(user.getName())
				.userRole(user.getUserRole())
				.build();
	}
}
