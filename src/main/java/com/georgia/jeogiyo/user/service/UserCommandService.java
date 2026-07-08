package com.georgia.jeogiyo.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.exception.UserDomainException;
import com.georgia.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;
	
	private final UserFinder userFinder;
	
	private final PasswordEncoder passwordEncoder;
	
	@Transactional(rollbackFor = Exception.class)
	public UserSignupResponse signup(UserSignupRequest signupUser) {
		if(userRepository.existsByEmail(signupUser.getEmail())) {
			// TODO: 이미 사용중인 이메일입니다. 409
			throw new UserDomainException();
		}
		
		if(userRepository.existsByNickname(signupUser.getNickname())) {
			// TODO: 이미 사용중인 닉네임입니다. 409
			throw new UserDomainException();
		}
		
		if(userRepository.existsByLoginId(signupUser.getLoginId())) {
			// TODO: 이미 사용중인 아이디입니다. 409
			throw new UserDomainException();			
		}
		
		User saved = userRepository.save(User.create(signupUser, passwordEncoder));
		
		return UserSignupResponse.of(saved);
	}
	
}
