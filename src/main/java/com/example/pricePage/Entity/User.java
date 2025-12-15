package com.example.pricePage.Entity;

import com.example.pricePage.Dto.UserDto;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String username;

    private String email ;

    @Enumerated(EnumType.STRING)
    private Role role;


    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;


    public void updateUser(UserDto userDto) {
        this.username= userDto.getUsername;
        this.role = userDto.getRole();
        this.modifiedDate = LocalDateTime.now();
        this.email = userDto.getEmail();

    }


}
