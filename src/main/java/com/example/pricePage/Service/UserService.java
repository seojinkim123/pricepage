package com.example.pricePage.Service;

import com.example.pricePage.Dto.UserDto;
import com.example.pricePage.Entity.User;
import com.example.pricePage.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto saveUser(UserDto userDto) {
        User user = userDto.toUser();
        User savedUser =userRepository.save(user);
        return UserDto.toUserDto(savedUser);

    }
    public UserDto updateUser(UserDto userDto) {
        Long userId= userDto.getId();
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        user.updateUser(userDto);
        return UserDto.toUserDto(user);
    }

    public UserDto findById(Long id){
        User user = userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
        return  UserDto.toUserDto(user);
    }



}
