package com.tanaka.asset.asset_manager.repository;

import com.tanaka.asset.asset_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // メールでログイン
}
