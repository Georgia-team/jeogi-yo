package com.georgia.jeogiyo.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class UserCommandTest {

	@Autowired
	private UserService userCommandService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserFinder userFinder;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	private UserSignupRequest userSignupRequest = UserFix.getUserSignupRequest();
	
	@Test
	@DisplayName("service: 유저 로그인 테스트")
	void userLoginTest() {
		userCommandService.signup(userSignupRequest);
		
		UserLoginRequest loginRequest = new UserLoginRequest(userSignupRequest.getLoginId(), userSignupRequest.getPassword());
		
		UserLoginResponse loginResponse = userCommandService.login(loginRequest);
		
		String accessToken = jwtUtil.subStringToken(loginResponse.getAccessToken());
		
		Claims claims = jwtUtil.getUserInfoFromToken(accessToken);
		
		String loginId = claims.getSubject();
		String claimsRole = claims.get(JwtUtil.AUTHORIZATION_KEY, String.class);
		Role role = Role.valueOf(claimsRole);
		
		User user = userFinder.getUserByLoginId(userSignupRequest.getLoginId());
		
		assertThat(loginId).isEqualTo(user.getLoginId());
		assertThat(role).isEqualTo(user.getRole());
		assertThat(jwtUtil.validateToken(accessToken)).isTrue();
	}
	
	@Test
	@DisplayName("service: 유저 생성 테스트")
	void userSignupTest() {
		UserSignupResponse response = userCommandService.signup(userSignupRequest);
		
		assertThat(response.getUserId()).isNotNull();
//		assertThatNoException().isThrownBy(() -> {
//			UUID.fromString(response.getUserId());
//		});
		
		
		assertThat(response.getCreatedAt()).isNotNull();
		assertThat(response.getLoginId()).isEqualTo(userSignupRequest.getLoginId());
		assertThat(response.getNickname()).isEqualTo(userSignupRequest.getNickname());
		assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
		assertThat(response.isDeleted()).isFalse();
		
		User user = userFinder.getUserById(response.getUserId());
		
		assertThat(user).isNotNull();
		assertThat(user.getCreatedAt()).isNotNull();
		
		// TODO: BaseEntity CreatedBy, UpdatedBy 완성 시 수정
		assertThat(user.getCreatedBy()).isEqualTo("GUEST");
		
		assertThat(user.getUpdatedAt()).isNull();
		
		// TODO: BaseEntity CreatedBy, UpdatedBy 완성 시 수정
		assertThat(user.getDeletedAt()).isNull();
		assertThat(user.getDeletedBy()).isNull();
	}
	
	@Test
	@DisplayName("service: 유저 이메일 수정 테스트")
	void userEmailUpdateTest() {
		UserSignupResponse given = userCommandService.signup(userSignupRequest);
		
		User before = userFinder.getUserById(given.getUserId());
		
		em.flush();
		em.clear();
		
		UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
				null,
				null,
				"test1234@email.com",
				null
		);
		
		UserInfoResponse response = userCommandService.update(given.getLoginId(), userUpdateRequest);
		
		// 응답에 포함된 Email과 요청시 보낸 Email과 똑같은지 확인
		assertThat(response.getEmail()).isEqualTo(userUpdateRequest.getEmail());
		
		em.flush();
		em.clear();
		
		User updated = userFinder.getUserById(given.getUserId());
		
		// 요청에 null 처리 된 부분이 변경되지 않았는지 확인
		assertThat(before.getNickname()).isEqualTo(updated.getNickname());
		assertThat(before.getPhone()).isEqualTo(updated.getPhone());
		assertThat(before.getPassword()).isEqualTo(updated.getPassword());
		
		// 요청에 포함된 Email이 변경 이전 Email과 다른지 확인
		assertThat(before.getEmail()).isNotEqualTo(updated.getEmail());
	}
	
	@Test
	@DisplayName("service: 유저 닉네임 수정 테스트")
	void userNicknameUpdateTest() {
		UserSignupResponse given = userCommandService.signup(userSignupRequest);
		
		User before = userFinder.getUserById(given.getUserId());
		
		em.flush();
		em.clear();
		
		UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
				"testnickname",
				null,
				null,
				null
		);
		
		UserInfoResponse response = userCommandService.update(given.getLoginId(), userUpdateRequest);
		
		// 응답에 포함된 Nickname과 요청시 보낸 Nickname과 똑같은지 확인
		assertThat(response.getNickname()).isEqualTo(userUpdateRequest.getNickname());
		
		em.flush();
		em.clear();
		
		User updated = userFinder.getUserById(given.getUserId());
		
		// 요청에 null 처리 된 부분이 변경되지 않았는지 확인
		assertThat(before.getEmail()).isEqualTo(updated.getEmail());
		assertThat(before.getPhone()).isEqualTo(updated.getPhone());
		assertThat(before.getPassword()).isEqualTo(updated.getPassword());
		
		// 요청에 포함된 Nickname이 변경 이전 Nickname과 다른지 확인
		assertThat(before.getNickname()).isNotEqualTo(updated.getNickname());
	}
	
	@Test
	@DisplayName("service: 유저 전화번호 수정 테스트")
	void userPhoneUpdateTest() {
		UserSignupResponse given = userCommandService.signup(userSignupRequest);
		
		User before = userFinder.getUserById(given.getUserId());
		
		em.flush();
		em.clear();
		
		UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
				null,
				"02-123-1234",
				null,
				null
		);
		
		UserInfoResponse response = userCommandService.update(given.getLoginId(), userUpdateRequest);
		
		assertThat(response.getPhone()).isEqualTo(userUpdateRequest.getPhone());
		
		em.flush();
		em.clear();
		
		User updated = userFinder.getUserById(given.getUserId());
		
		// 요청에 null 처리 된 부분이 변경되지 않았는지 확인
		assertThat(before.getEmail()).isEqualTo(updated.getEmail());
		assertThat(before.getPassword()).isEqualTo(updated.getPassword());
		assertThat(before.getNickname()).isEqualTo(updated.getNickname());
		
		// 요청에 포함된 Phone이 변경 이전 Phone과 다른지 확인
		assertThat(before.getPhone()).isNotEqualTo(updated.getPhone());
	}
	
	@Test
	@DisplayName("service: 유저 패스워드 수정 테스트")
	void userPasswordUpdateTest() {
		UserSignupResponse given = userCommandService.signup(userSignupRequest);
		
		User before = userFinder.getUserById(given.getUserId());
		
		em.flush();
		em.clear();
		
		UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
				null,
				null,
				null,
				"Test123456789@"
		);
		
		userCommandService.update(given.getLoginId(), userUpdateRequest);
		
		em.flush();
		em.clear();
		
		User updated = userFinder.getUserById(given.getUserId());
		
		assertThat(before.getEmail()).isEqualTo(updated.getEmail());
		assertThat(before.getNickname()).isEqualTo(updated.getNickname());
		assertThat(before.getPhone()).isEqualTo(updated.getPhone());
		
		assertThat(before.getPassword()).isNotEqualTo(updated.getPassword());
		assertThat(passwordEncoder.matches(userUpdateRequest.getPassword(), updated.getPassword())).isTrue();
	}
	
	@Test
	@DisplayName("service: 유저 탈퇴 테스트")
	void userDeleteTest() {
		UserSignupResponse given = userCommandService.signup(userSignupRequest);
		
		String email = given.getEmail();
		String password = userSignupRequest.getPassword();
		
		em.flush();
		em.clear();
		
		UserDeleteRequest userDeleteRequest = new UserDeleteRequest(email, password);
		
		UserDeleteResponse userDeleteResponse = userCommandService.delete(given.getLoginId(), userDeleteRequest);
		
		assertThat(userDeleteResponse.getUserId()).isEqualTo(given.getUserId());
		
		em.flush();
		em.clear();
		
		User updated = userFinder.getUserById(userDeleteResponse.getUserId());
		
		assertThat(updated.getDeletedAt()).isNotNull();
		assertThat(updated.getDeletedBy()).isEqualTo(given.getLoginId());
		assertThat(updated.isDeleted()).isTrue();
	}
	
}