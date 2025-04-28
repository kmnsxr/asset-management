package com.tanaka.asset.asset_manager.service;

import com.tanaka.asset.asset_manager.entity.User;
import com.tanaka.asset.asset_manager.repository.UserRepository;
import com.tanaka.asset.asset_manager.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DBからユーザー情報を取得
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

        // CustomUserDetails を返す
        return CustomUserDetails.fromUser(user);
    }
}
