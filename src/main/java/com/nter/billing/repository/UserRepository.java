package com.nter.billing.repository;

import com.nter.billing.entity.UserJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserJPA, Long> {

    Optional<UserJPA> findByUsername(String username);
}
