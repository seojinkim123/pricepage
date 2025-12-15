package com.example.pricePage.Dto;


import com.example.pricePage.Entity.Role;
import com.example.pricePage.Entity.User;
import lombok.Data;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

@Data
public class UserDto {

    public String getUsername;
    private Long id;
    private String email;
    private String username;

    private Role role;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    private static ModelMapper modelMapper = new ModelMapper();

    public User toUser(){
        return modelMapper.map(this,User.class);
    }
    public static UserDto toUserDto(User user){
        return modelMapper.map(user,UserDto.class);
    }


}
