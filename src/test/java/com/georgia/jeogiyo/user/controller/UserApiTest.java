package com.georgia.jeogiyo.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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
import com.georgia.jeogiyo.user.dto.request.UserDeleteRequest;
import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
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
public class UserApiTest {

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
		userLoginId = userService.signup(userSignupRequest, Role.CUSTOMER).getLoginId();
		user = userFinder.getUserByLoginId(userLoginId);
		
		em.flush();
		em.clear();
	}
	
	@Test
	@DisplayName("API: 회원가입 테스트")
	void userSignupApiTest_Customer() throws Exception {
		String url = "/api/v1/auth/signup";
		
		UserSignupRequest userSignupRequest = new UserSignupRequest(
				"apitest01",
				"Password123@",
				"nickckcik",
				"02-000-0000",
				"test@tested.com"
		);
		
		mockMvc
		.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userSignupRequest))
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.email").value(userSignupRequest.getEmail()))
		.andExpect(jsonPath("$.loginId").value(userSignupRequest.getLoginId()))
		.andExpect(jsonPath("$.nickname").value(userSignupRequest.getNickname()))
		.andExpect(jsonPath("$.role").value(Role.CUSTOMER.name()))
		.andExpect(jsonPath("$.deleted").value(false))
		;
	}
	
	@Test
	@DisplayName("API: 회원가입 테스트")
	void userSignupApiTest_Onwer() throws Exception {
		String url = "/api/v1/auth/signup/owner";
		
		UserSignupRequest userSignupRequest = new UserSignupRequest(
				"apitest01",
				"Password123@",
				"nickckcik",
				"02-000-0000",
				"test@tested.com"
				);
		
		mockMvc
		.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userSignupRequest))
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.email").value(userSignupRequest.getEmail()))
		.andExpect(jsonPath("$.loginId").value(userSignupRequest.getLoginId()))
		.andExpect(jsonPath("$.nickname").value(userSignupRequest.getNickname()))
		.andExpect(jsonPath("$.role").value(Role.OWNER.name()))
		.andExpect(jsonPath("$.deleted").value(false))
		;
	}
	
	@Test
	@DisplayName("API: 로그인 테스트")
	void userLoginApiTest() throws Exception {
		String url = "/api/v1/auth/login";
		
		UserLoginRequest userLoginRequest = new UserLoginRequest(
				userSignupRequest.getLoginId(),
				userSignupRequest.getPassword()
		);
		
		mockMvc
		.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userLoginRequest))
		)
		.andExpect(status().isOk())
		.andExpect(cookie().exists("Authorization"))
		// JwtAuthenticationFilter 쪽 로그인 구현 상태상 응답 바디에 아래 내용이 포함되지 않음.
//		.andExpect(jsonPath("$.accessToken").isNotEmpty())
//		.andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
//		.andExpect(jsonPath("$.loginId").value(user.getLoginId()))
//		.andExpect(jsonPath("$.nickname").value(user.getNickname()))
//		.andExpect(jsonPath("$.role").value(user.getRole().name()))
		;
	}
	
	@Test
	@DisplayName("API: 내 정보 수정 테스트")
	void userUpdateApiTest() throws Exception {
		String url = "/api/v1/users/me";
		
		UserUpdateRequest userUpdateRequest = UserFix.getUserUpdateRequest();
		
		// 403 : JWT AccessToken 쿠키 없음
		mockMvc
		.perform(patch(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userUpdateRequest))
		)
		.andExpect(status().isForbidden())
		;
		
		UserLoginRequest userLoginRequest = new UserLoginRequest(
				userSignupRequest.getLoginId(),
				userSignupRequest.getPassword()
		);
		
		UserLoginResponse loginResponse = userService.login(userLoginRequest);
		
		mockMvc
		.perform(patch(url)
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.content(objectMapper.writeValueAsString(userUpdateRequest))
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
		.andExpect(jsonPath("$.loginId").value(user.getLoginId()))
		.andExpect(jsonPath("$.nickname").value(user.getNickname()))
		.andExpect(jsonPath("$.phone").value(userUpdateRequest.getPhone()))
		.andExpect(jsonPath("$.email").value(userUpdateRequest.getEmail()))
		.andExpect(jsonPath("$.role").value(user.getRole().toString()))
		;
	}
	
	@Test
	@DisplayName("API: 회원 삭제 테스트")
	void userDeleteApiTest() throws Exception {
		String url = "/api/v1/users/me";
		
		UserDeleteRequest userDeleteRequest = new UserDeleteRequest(
				userSignupRequest.getEmail(),
				userSignupRequest.getPassword()
		);
		
		// 403
		mockMvc
		.perform(delete(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userDeleteRequest))
		)
		.andExpect(status().isForbidden())
		;
		
		UserLoginRequest userLoginRequest = new UserLoginRequest(
				userSignupRequest.getLoginId(),
				userSignupRequest.getPassword()
		);
		
		UserLoginResponse loginResponse = userService.login(userLoginRequest);
		
		mockMvc
		.perform(delete(url)
				.contentType(MediaType.APPLICATION_JSON)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.content(objectMapper.writeValueAsString(userDeleteRequest))
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.userId").value(user.getUserId().toString()))
		;
	}
}
