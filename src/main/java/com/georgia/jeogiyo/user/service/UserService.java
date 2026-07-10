package com.georgia.jeogiyo.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.global.jwt.JwtUtil;
import com.georgia.jeogiyo.user.dto.request.UserDeleteRequest;
import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
import com.georgia.jeogiyo.user.dto.response.UserDeleteResponse;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.dto.response.UserLoginResponse;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.exception.UserDomainException;
import com.georgia.jeogiyo.user.exception.UserErrorCode;
import com.georgia.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	
	private final UserFinder userFinder;
	
	private final JwtUtil jwtUtil;
	
	private final PasswordEncoder passwordEncoder;
	
	private final UserInfoUpdateService userInfoUpdateService;
	
	@Transactional(rollbackFor = Exception.class)
	public UserLoginResponse login(UserLoginRequest userLogin) {
		User user = userFinder.getUserByLoginId(userLogin.getLoginId());
		
		if(!passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
			throw new UserDomainException(UserErrorCode.NOT_FOUND_USER);
		}
		
		String accessToken = jwtUtil.createToken(user.getLoginId(), user.getRole());
		
		return UserLoginResponse.of(user, accessToken);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public UserSignupResponse signup(UserSignupRequest signupUser) {
		if(userRepository.existsByEmail(signupUser.getEmail())) {
			// TODO: 이미 사용중인 이메일입니다. 409
			throw new UserDomainException(UserErrorCode.DUPLICATE_EMAIL);
		}
		
		if(userRepository.existsByNickname(signupUser.getNickname())) {
			// TODO: 이미 사용중인 닉네임입니다. 409
			throw new UserDomainException(UserErrorCode.DUPLICATE_NICKNAME);
		}
		
		if(userRepository.existsByLoginId(signupUser.getLoginId())) {
			// TODO: 이미 사용중인 아이디입니다. 409
			throw new UserDomainException(UserErrorCode.DUPLICATION_LOGIN_ID);
		}
		
		User saved = userRepository.save(User.create(signupUser, passwordEncoder));
		
		return UserSignupResponse.of(saved);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public UserInfoResponse update(String loginId, UserUpdateRequest updateUser) {
		User user = userFinder.getUserByLoginId(loginId);
		
		List<String> updateFields = updateUser.getUpdateFields();
		
		updateFields.stream().forEach(field -> {
			switch (field) {
				case "nickname" -> userInfoUpdateService.changeNickname(user, updateUser.getNickname());
				case "phone" -> userInfoUpdateService.changePhone(user, updateUser.getPhone());
				case "email" -> userInfoUpdateService.changeEmail(user, updateUser.getEmail());
				case "password" -> userInfoUpdateService.changePassword(user, updateUser.getPassword(), passwordEncoder);
				
				// TODO: 업데이트를 진행할 수 없는 정보입니다.
				default -> throw new UserDomainException(UserErrorCode.UPDATE_FAILURE);
			}
		});
		
		return UserInfoResponse.of(user);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public UserDeleteResponse delete(String loginId, UserDeleteRequest deleteUser) {
		User user = userFinder.getUserByLoginId(loginId);
		
		// 1. 요청에 담긴 email, password 일치 검증
		if(user.verifyCredentialsForDelete(deleteUser, passwordEncoder)) {
			// 2. 성공 시 Delete
			user.softDelete(user.getLoginId());
		} else {
			// 3. 실패 시
			// TODO: 회원탈퇴를 진행할 수 없습니다.
			throw new UserDomainException(UserErrorCode.DELETE_FAILURE);
		}
		
		User deleted = userRepository.save(user);
		
		return UserDeleteResponse.of(deleted);
	}
	
}
