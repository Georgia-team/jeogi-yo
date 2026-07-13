package com.georgia.jeogiyo.global.security;

import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * ─────────────────────
 * 작성자: 진혜림
 * 작성일: 2026-07-09
 * 설명: 인증 객체 생성
 * ─────────────────────
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginIdAndIsDeletedFalse(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("Not Found " + loginId));

        return new UserDetailsImpl(user);
    }
}
