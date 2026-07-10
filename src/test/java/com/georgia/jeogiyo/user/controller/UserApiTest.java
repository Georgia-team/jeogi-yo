package com.georgia.jeogiyo.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.service.UserFinder;
import com.georgia.jeogiyo.user.service.UserService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserApiTest {

	@Autowired
	private MockMvc mockMvc;
	
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	
	@Autowired
	private UserAuthController userAuthController;
	
	@Autowired
	private UserCommandController userCommandController;
	
	@Autowired
	private UserQueryController userQueryController;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserFinder userFinder;
	
	private UserSignupRequest userSignup;
	
	private String userLoginId;
	
	private User user;
	
	@BeforeEach
	void setUp() {
		userSignup = UserFix.getUserSignupRequest();
		userLoginId = userService.signup(userSignup).getLoginId();
		user = userFinder.getUserByLoginId(userLoginId);
		
		em.flush();
		em.clear();
	}
	
	@Test
	@DisplayName("API: 회원가입 테스트")
	void userSignupApiTest() throws Exception {
		UserSignupRequest userSignupRequest = new UserSignupRequest(
				"apitest01",
				"PasswordA@",
				"nickckcik",
				"02-000-0000",
				"test@tested.com"
		);
		
		mockMvc
		.perform(post("/api/v1/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userSignupRequest))
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.email").value(userSignupRequest.getEmail()))
		.andExpect(jsonPath("$.loginId").value(userSignupRequest.getLoginId()))
		;
	}
	
	@Test
	@DisplayName("API: 로그인 테스트")
	void userLoginApiTest() throws Exception {
		
	}
	
}
