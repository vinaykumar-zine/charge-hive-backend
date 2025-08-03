package com.vin.server.dto;

import com.vin.server.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRegistrationReq {

	private String name;
	private String email;
	private String password;
	private UserRole userRole;
}
