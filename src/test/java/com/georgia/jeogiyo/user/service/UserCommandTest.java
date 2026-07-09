package com.georgia.jeogiyo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;

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
	
	private UserSignupRequest userSignup = UserFix.getUserCreateRequest();
	
	@Test
	@DisplayName("service: 유저 생성 테스트")
	void userSignupTest() {
		UserSignupResponse response = userCommandService.signup(userSignup);
		
		assertThat(response.getUserId()).isNotNull();
//		assertThatNoException().isThrownBy(() -> {
//			UUID.fromString(response.getUserId());
//		});
		
		
		assertThat(response.getCreatedAt()).isNotNull();
		assertThat(response.getLoginId()).isEqualTo(userSignup.getLoginId());
		assertThat(response.getNickname()).isEqualTo(userSignup.getNickname());
		assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
		assertThat(response.isDeleted()).isFalse();
		
		User user = userFinder.getUserById(response.getUserId());
		
		assertThat(user).isNotNull();
		assertThat(user.getCreatedAt()).isNotNull();
		
		// TODO: BaseEntity CreatedBy, UpdatedBy 완성 시 수정
		assertThat(user.getCreatedBy()).isEqualTo("user");
		
		assertThat(user.getUpdatedAt()).isNotNull();
		
		// TODO: BaseEntity CreatedBy, UpdatedBy 완성 시 수정
		assertThat(user.getUpdatedBy()).isEqualTo("user");
		assertThat(user.getDeletedAt()).isNull();
		assertThat(user.getDeletedBy()).isNull();
	}
	
	@Test
	@DisplayName("service: 유저 수정 테스트")
	void userUpdateTest() {
		UserSignupResponse given = userCommandService.signup(userSignup);
		
		em.flush();
		em.clear();
		
		UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
				"nana",
				"02-123-1234",
				null,
				"Test123456789@"
		);
		
		UserInfoResponse response = userCommandService.update(given.getUserId(), userUpdateRequest);
		
		assertThat(response.getNickname()).isEqualTo(userUpdateRequest.getNickname());
		assertThat(response.getPhone()).isEqualTo(userUpdateRequest.getPhone());
		
		em.flush();
		em.clear();
		
		User updated = userFinder.getUserById(given.getUserId());
		
		assertThat(updated.getEmail()).isEqualTo(given.getEmail());
		assertThat(passwordEncoder.matches(userUpdateRequest.getPassword(), updated.getPassword()));
	}
	
}