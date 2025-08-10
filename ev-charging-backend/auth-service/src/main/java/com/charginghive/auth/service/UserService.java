// The service layer in Spring Boot responsible for handling user-related operations such as registration, token generation, user management, etc.
package com.charginghive.auth.service;

// Importing necessary DTOs (Data Transfer Objects) used to transfer user-related data between layers.
import com.charginghive.auth.dto.UserDto;
import com.charginghive.auth.dto.UserEdirDto;
import com.charginghive.auth.dto.UserResDto;
import com.charginghive.auth.dto.ChangePasswordRequest;
import com.charginghive.auth.dto.ForgotPasswordRequest;
import com.charginghive.auth.dto.ResetPasswordRequest;
import com.charginghive.auth.entity.UserRole;

// Utility classes and Spring Framework dependencies
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final JwtUtils jwtUtils;

    // simple in-memory token stores for demo (replace with persistent store in production)
    private final Map<String, Long> passwordResetTokens = new HashMap<>();
    private final Map<String, Long> emailVerificationTokens = new HashMap<>();

	/**
	 * Registers a new user in the system.
	 * @param credential UserRegistrationReq DTO containing user signup details.
	 * @return Success or failure message based on outcome.
	 */
	public UserDto saveUserDetails(UserRegistrationReq credential) {
//		String mesg = "user regestration failed!";
//		try {
//			// Logging raw password for debug purposes (not recommended in production)
//			// System.out.println("user passowrd is: " + credential.getPassword());
//
//			// Encode the password before storing it in DB
//			credential.setPassword(passwordEncoder.encode(credential.getPassword()));
//
//			// Convert DTO to Entity and save to database
//			UserRegistration dbUser = repository.save(modelMapper.map(credential, UserRegistration.class));
//            return modelMapper.map(dbUser, UserDto.class);

			try {
				credential.setPassword(passwordEncoder.encode(credential.getPassword()));
				UserRegistration userEntity = modelMapper.map(credential, UserRegistration.class);
				UserRegistration savedUser = repository.save(userEntity);
				return modelMapper.map(savedUser, UserDto.class);
			} catch (Exception ex) {
				System.out.println("Error occurred while registering user: " + ex.getMessage());
				throw new RuntimeException("User registration failed");
			}


//
//		} catch (Exception ex) {
//			// Exception handling with logging
//			System.out.println("Error occured during registering user " + ex.getMessage());
//		}
//		return null;
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

	public String editUserDetails(UserEdirDto credential, Long id) {
		String msg = "User update failed!";
		try {
			// Fetch the user securely using their ID or throw exception if not found
			UserRegistration user = repository.findById(id)
					.orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

			// Update name, email and encoded password
			user.setFirstName(credential.getFirstName());
			user.setLastName(credential.getLastName());
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


    /*
       new endpoints added for password reset and email verification
     */



    public void changePassword(Long userId, ChangePasswordRequest req) {
        UserRegistration user = repository.findById(userId)
		// add a custom exception for user not found
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            //make a custom exception for password mismatch
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        repository.save(user);
    }

    // public void requestPasswordReset(ForgotPasswordRequest req) {
    //     UserRegistration user = repository.findByEmail(req.getEmail())
    //             .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + req.getEmail()));
    //     String token = java.util.UUID.randomUUID().toString();
    //     passwordResetTokens.put(token, user.getId());
    //     // TODO: send email with token
    // }

    // public void resetPassword(ResetPasswordRequest req) {
    //     Long userId = passwordResetTokens.remove(req.getToken());
    //     if (userId == null) throw new RuntimeException("Invalid or expired token");
    //     UserRegistration user = repository.findById(userId)
    //             .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    //     user.setPassword(passwordEncoder.encode(req.getNewPassword()));
    //     repository.save(user);
    // }

//    public void requestEmailVerification(String email) {
//        UserRegistration user = repository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
//        String token = java.util.UUID.randomUUID().toString();
//        emailVerificationTokens.put(token, user.getId());
//        // TODO: send email with token
//    }
//
//    public void confirmEmailVerification(String token) {
//        Long userId = emailVerificationTokens.remove(token);
//        if (userId == null) throw new RuntimeException("Invalid or expired token");
//        // no emailVerified flag in entity yet; would add in production
//    }


	public UserDto getById(Long id) {
		UserRegistration user = repository.findById(id).orElseThrow(() -> new RuntimeException("User not found with ID: " + id)); //add a custom exception for user not found
		return modelMapper.map(user, UserDto.class);
	}

	/**
	 * Check if a user exists by ID - required by booking service
	 */
	public boolean userExists(Long id) {
		return repository.existsById(id);
	}

	public UserResDto mapToDto(UserRegistration user) {
		return UserResDto.builder()
				.email(user.getEmail())
				.id(user.getId())
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.userRole(user.getUserRole())
				.build();

	}
}
