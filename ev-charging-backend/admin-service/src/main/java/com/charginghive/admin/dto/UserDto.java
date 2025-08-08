package com.charginghive.admin.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    //changed role to userRole
    private String userRole;
}
