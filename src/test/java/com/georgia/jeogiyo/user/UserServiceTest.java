package com.georgia.jeogiyo.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.georgia.jeogiyo.user.dto.UserSignupRequest;
import com.georgia.jeogiyo.user.service.UserCommandService;
import com.georgia.jeogiyo.user.service.UserFinder;

@SpringBootTest
public class UserServiceTest {

	@Autowired
	private UserCommandService userCommand;
	
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	
	@Autowired
	private UserFinder userFinder;
	
	private UserSignupRequest userSignup = UserFix.getUserCreateRequest();
	
	@Test
	@DisplayName("service: 유저 생성 테스트")
	void userSignupTest() {
		
	}
	
}