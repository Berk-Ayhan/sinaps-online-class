package com.sinaps.onlineclass.dto;

import com.sinaps.onlineclass.enums.Role;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {

    private Integer id;

    private String firstName;

    private String lastName;

    private String username;

    private String password;

    private Role role;

}
