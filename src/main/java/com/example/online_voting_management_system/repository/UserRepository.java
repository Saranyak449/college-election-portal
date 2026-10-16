package com.example.online_voting_management_system.repository;

import com.example.online_voting_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByVoterId(String voterId);
    Optional<User> findByEmail(String email);
}