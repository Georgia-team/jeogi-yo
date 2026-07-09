package com.georgia.jeogiyo.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.exception.UserDomainException;
import com.georgia.jeogiyo.user.exception.UserErrorCode;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class UserCommandFailureTest {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userCommandService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserFinder userFinder;
	
	@Autowired
	private EntityManager em;
	
	private UserSignupRequest userSignupRequest = UserFix.getUserCreateRequest();
	
	private User user;
	
	private UUID userId;
	
	@BeforeEach
	void setUp() {
		user = userRepository.save(User.create(userSignupRequest, passwordEncoder));
		
		userId = user.getUserId();
	}
	
	@Test
	@DisplayName("service-fail: 회원가입 테스트 실패 케이스: 중복 이메일")
	void failSignupTest_Email() {
		UserSignupRequest failSignupRequestEmail = new UserSignupRequest(
				"failtest01",
				"Password01@",
				"failnickname",
				userSignupRequest.getPhone(),
				userSignupRequest.getEmail()
		);
		
		assertThatThrownBy(() -> userCommandService.signup(failSignupRequestEmail))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_EMAIL.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원가입 테스트 실패 케이스: 중복 닉네임")
	void failSignupTest_Nickname() {
		UserSignupRequest failSignupRequestNickname = new UserSignupRequest(
				"failtest01",
				"Password01@",
				userSignupRequest.getNickname(),
				userSignupRequest.getPhone(),
				"failtest@email.com"
		);
		
		assertThatThrownBy(() -> userCommandService.signup(failSignupRequestNickname))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원가입 테스트 실패 케이스: 중복 아이디")
	void failSignupTest_LoginId() {
		UserSignupRequest failSignupRequestNickname = new UserSignupRequest(
				userSignupRequest.getLoginId(),
				"Password01@",
				"failnickname",
				userSignupRequest.getPhone(),
				"failtest@email.com"
		);
		
		assertThatThrownBy(() -> userCommandService.signup(failSignupRequestNickname))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATION_LOGIN_ID.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원 수정 테스트 실패 케이스: 중복 닉네임")
	void failUpdateTest_Nickname() {
		UserUpdateRequest failUpdateRequestNickname = new UserUpdateRequest(
				userSignupRequest.getNickname(),
				null, null, null
		);
		
		assertThatThrownBy(() -> userCommandService.update(user.getLoginId(), failUpdateRequestNickname))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원 수정 테스트 실패 케이스: 중복 이메일")
	void failUpdateTest_Email() {
		UserSignupRequest userSignupRequestGiven = new UserSignupRequest(
				"test0000",
				"password1234A@",
				"springboot",
				"02-000-0000",
				"failtest@email.com"
		);
		
		userCommandService.signup(userSignupRequestGiven);
		
		User given = userFinder.getUserByLoginId(userSignupRequestGiven.getLoginId());
		
		UserUpdateRequest failUpdateRequestEmail = new UserUpdateRequest(
				null, null, userSignupRequest.getEmail(), null
		);
		
		assertThatThrownBy(() -> userCommandService.update(given.getLoginId(), failUpdateRequestEmail))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_EMAIL.getMessage());
	}
	
}
