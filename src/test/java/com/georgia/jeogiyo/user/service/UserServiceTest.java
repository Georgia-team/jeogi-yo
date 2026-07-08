package com.georgia.jeogiyo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;

@SpringBootTest
@Import(UserServiceTest.TestConfig.class)
public class UserServiceTest {

	@Autowired
	private UserService userCommandService;
	
	@Autowired
	private UserFinder userFinder;
	
	private UserSignupRequest userSignup = UserFix.getUserCreateRequest();
	
	@TestConfiguration
	static class TestConfig {
		// 임시 PasswordEncoder Bean
		// TODO: PasswordEncoder Bean 추가 시 삭제
		@Bean
		PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}
	}
	
	@Test
	@DisplayName("service: 유저 생성 테스트")
	void userSignupTest() {
		UserSignupResponse response = userCommandService.signup(userSignup);
		
		assertThat(response.getUserId()).isNotNull();
		assertThatNoException().isThrownBy(() -> {
			UUID.fromString(response.getUserId());
		});
		
		assertThat(response.getCreatedAt()).isNotNull();
		assertThat(response.getLoginId()).isEqualTo(userSignup.getLoginId());
		assertThat(response.getNickname()).isEqualTo(userSignup.getNickname());
		assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
		assertThat(response.isDeleted()).isFalse();
		
		User user = userFinder.getUserById(response.getUserId());
		
		assertThat(user).isNotNull();
		assertThat(user.getCreatedAt()).isNotNull();
		assertThat(user.getCreatedBy()).isEqualTo(userSignup.getLoginId());
		assertThat(user.getUpdatedAt()).isNotNull();
		assertThat(user.getUpdatedBy()).isEqualTo(userSignup.getLoginId());
		assertThat(user.getDeletedAt()).isNull();
		assertThat(user.getDeletedBy()).isNull();
	}
	
}