package com.georgia.jeogiyo.user.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.dto.request.UserSearchRequest;
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
		.andExpect(status().isUnauthorized())
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
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.message").value("내 정보 조회 성공"))
		
		.andExpect(jsonPath("$.data.userId").value(user.getUserId().toString()))
		.andExpect(jsonPath("$.data.loginId").value(user.getLoginId()))
		.andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
		.andExpect(jsonPath("$.data.phone").value(user.getPhone()))
		.andExpect(jsonPath("$.data.email").value(user.getEmail()))
		.andExpect(jsonPath("$.data.role").value(user.getRole().name()))
		;
	}
	
	@Test
	@DisplayName("API: 회원 목록 검색")
	void searchUserListTest() throws Exception {
		List<UserSignupRequest> testUserListNameTest = List.of(
				new UserSignupRequest("test01", "Password01@", "nickname01", "02-000-0000", "test01@email.com"),
				new UserSignupRequest("test02", "Password01@", "nickname02", "02-000-0000", "test02@email.com"),
				new UserSignupRequest("test03", "Password01@", "nickname03", "02-000-0000", "test03@email.com"),
				new UserSignupRequest("test04", "Password01@", "nickname04", "02-000-0000", "test04@email.com"),
				new UserSignupRequest("test05", "Password01@", "nickname05", "02-000-0000", "test05@email.com"),
				new UserSignupRequest("test06", "Password01@", "nickname06", "02-000-0000", "test06@email.com"),
				new UserSignupRequest("test07", "Password01@", "nickname07", "02-000-0000", "test07@email.com"),
				new UserSignupRequest("test08", "Password01@", "nickname08", "02-000-0000", "test08@email.com"),
				new UserSignupRequest("test09", "Password01@", "nickname09", "02-000-0000", "test09@email.com"),
				new UserSignupRequest("test10", "Password01@", "nickname10", "02-000-0000", "test10@email.com"),
				new UserSignupRequest("test11", "Password01@", "nickname11", "02-000-0000", "test11@email.com"),
				new UserSignupRequest("test12", "Password01@", "nickname12", "02-000-0000", "test12@email.com")
		);
		testUserListNameTest.stream()
		.forEach(u -> {
			userService.signup(u, Role.CUSTOMER);
		});
		
		em.flush();
		em.clear();
		
		String url = "/api/v1/users";
		
		UserLoginRequest userLoginRequest = new UserLoginRequest(
				userSignupRequest.getLoginId(),
				userSignupRequest.getPassword()
		);
		
		UserLoginResponse loginResponse = userService.login(userLoginRequest);
		
		UserSearchRequest searchRequest = new UserSearchRequest();
		
		searchRequest.setPage(0);
		searchRequest.setSize(10);
		searchRequest.setRole(Role.CUSTOMER);
		searchRequest.setKeyword("test");
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("role", searchRequest.getRole().name());
		params.add("keyword", searchRequest.getKeyword());
		params.add("page", String.valueOf(searchRequest.getPage()));
		params.add("size", String.valueOf(searchRequest.getSize()));
		
		mockMvc
		.perform(get(url)
				.contentType(MediaType.APPLICATION_JSON)
				.params(params)
		)
		.andExpect(status().isUnauthorized())
		;
		
		mockMvc
		.perform(get(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.params(params)
		)
		.andExpect(status().isOk())
		// CommonResponse
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.message").value("유저 목록 조회 성공"))
		
		// PageResponse
		.andExpect(jsonPath("$.data.content", hasSize(10)))
		.andExpect(jsonPath("$.data.page").value(0))
		.andExpect(jsonPath("$.data.size").value(10))
		.andExpect(jsonPath("$.data.totalElements").value(testUserListNameTest.size()))
		.andExpect(jsonPath("$.data.totalPages").value(2))
		;
	}
	
}
