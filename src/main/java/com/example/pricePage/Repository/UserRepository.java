package com.example.pricePage.Repository;

import com.example.pricePage.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long>{
}
