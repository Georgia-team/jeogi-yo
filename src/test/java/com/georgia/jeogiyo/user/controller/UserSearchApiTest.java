package com.georgia.jeogiyo.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserLoginResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.service.UserFinder;
import com.georgia.jeogiyo.user.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserSearchApiTest {

	@Autowired
	private MockMvc mockMvc;
	
	private final ObjectMapper objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserFinder userFinder;
	
	private UserSignupRequest userSignupRequest;
	
	private String userLoginId;
	
	private User user;
	
	@BeforeEach
	void setUp() {
		userSignupRequest = UserFix.getUserSignupRequest();
		userLoginId = userService.signup(userSignupRequest).getLoginId();
		user = userFinder.getUserByLoginId(userLoginId);
		user.changeRole(Role.MASTER);
		
		em.flush();
		em.clear();
	}
	
	@Test
	@DisplayName("API: 내 정보 조회")
	void searchUserMeTest() throws Exception {
		String url = "/api/v1/users/me";
		
		// 403
		mockMvc
		.perform(get(url)
				.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isForbidden())
		;
		
		UserLoginRequest userLoginRequest = new UserLoginRequest(
				userSignupRequest.getLoginId(),
				userSignupRequest.getPassword()
		);
		
		UserLoginResponse loginResponse = userService.login(userLoginRequest);
		
		mockMvc
		.perform(get(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
		.andExpect(jsonPath("$.loginId").value(user.getLoginId()))
		.andExpect(jsonPath("$.nickname").value(user.getNickname()))
		.andExpect(jsonPath("$.phone").value(user.getPhone()))
		.andExpect(jsonPath("$.email").value(user.getEmail()))
		.andExpect(jsonPath("$.role").value(user.getRole().name()))
		;
	}
	
}
