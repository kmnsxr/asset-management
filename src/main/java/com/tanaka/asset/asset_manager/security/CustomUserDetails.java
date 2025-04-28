package com.tanaka.asset.asset_manager.security;

import com.tanaka.asset.asset_manager.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private String email;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String username, String password, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                Collections.singletonList(new SimpleGrantedAuthority("USER"))  // 権限設定（例: "USER"）
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 有効期限が切れていない場合はtrue
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // ロックされていない場合はtrue
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 資格情報が期限切れでない場合はtrue
    }

    @Override
    public boolean isEnabled() {
        return true;  // 有効なアカウントであればtrue
    }
}
