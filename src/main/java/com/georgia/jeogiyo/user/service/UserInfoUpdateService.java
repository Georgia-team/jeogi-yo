package com.georgia.jeogiyo.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserInfoUpdateService {
	
	private final UserRepository userRepository;
	
	void changeNickname(User user, String newNickname) {
		if(userRepository.existsByNickname(newNickname)) {
			// TODO: 이미 사용중인 닉네임입니다.
			throw new BusinessException(GlobalErrorCode.DUPLICATE_NICKNAME);
		}
		
		user.changeNickname(newNickname);
	}
	
	void changePhone(User user, String newPhone) {
		user.changePhone(newPhone);
	}
	
	void changeEmail(User user, String newEmail) {
		if(userRepository.existsByEmail(newEmail)) {
			// TODO: 이미 사용중인 이메일입니다.
			throw new BusinessException(GlobalErrorCode.DUPLICATE_EMAIL);
		}
		
		user.changeEmail(newEmail);
	}
	
	void changePassword(User user, String newPassword, PasswordEncoder passwordEncoder) {
		user.changePassword(newPassword, passwordEncoder);
	}
	
}
