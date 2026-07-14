package com.georgia.jeogiyo.address.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.request.AddressUpdateRequest;
import com.georgia.jeogiyo.address.service.AddressService;
import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserLoginResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AddressApiTest {

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
	private AddressService addressService;
	
	private UserSignupRequest userSignupRequest;
	
	private String loginId;
	
	@BeforeEach
	void setUp() {
		userSignupRequest = UserFix.getUserSignupRequest();
		loginId = userService.signup(userSignupRequest, Role.CUSTOMER).getLoginId();
		
		em.flush();
		em.clear();
	}
	
	@Test
	@DisplayName("API: 배송지 등록 API 테스트")
	void addressCreateApiTest() throws Exception {
		String url = "/api/v1/address";
		
		AddressCreateRequest request = createAddressRequest("Seoul Gangnam Teheran-ro 123", "101-1001", "06234", true);
		
		mockMvc
		.perform(post(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		)
		.andExpect(status().isUnauthorized())
		;
		
		UserLoginResponse loginResponse = login();
		
		mockMvc
		.perform(post(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.data.addressId").isNotEmpty())
		.andExpect(jsonPath("$.data.roadAddress").value(request.getRoadAddress()))
		.andExpect(jsonPath("$.data.detailAddress").value(request.getDetailAddress()))
		.andExpect(jsonPath("$.data.zipcode").value(request.getZipcode()))
		.andExpect(jsonPath("$.data.isDefault").value(true))
		.andExpect(jsonPath("$.data.createdAt").isNotEmpty())
		;
	}
	
	@Test
	@DisplayName("API: 배송지 수정 API 테스트")
	void addressUpdateApiTest() throws Exception {
		UUID addressId = addressService.addressCreate(loginId,
				createAddressRequest("Seoul Gangnam Teheran-ro 123", "101-1001", "06234", true)
		).getAddressId();
		
		em.flush();
		em.clear();
		
		String url = "/api/v1/address/" + addressId;
		
		AddressUpdateRequest request = new AddressUpdateRequest(
				"Seoul Gangnam Teheran-ro 234",
				"102-1002",
				"06235",
				true
		);
		
		mockMvc
		.perform(patch(url)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		)
		.andExpect(status().isUnauthorized())
		;
		
		UserLoginResponse loginResponse = login();
		
		mockMvc
		.perform(patch(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.data.addressId").value(addressId.toString()))
		.andExpect(jsonPath("$.data.roadAddress").value(request.getRoadAddress()))
		.andExpect(jsonPath("$.data.detailAddress").value(request.getDetailAddress()))
		.andExpect(jsonPath("$.data.zipcode").value(request.getZipcode()))
		.andExpect(jsonPath("$.data.isDefault").value(true))
		;
	}
	
	@Test
	@DisplayName("API: 배송지 삭제 API 테스트")
	void addressDeleteApiTest() throws Exception {
		addressService.addressCreate(loginId,
				createAddressRequest("Seoul Gangnam Teheran-ro 123", "101-1001", "06234", true)
		);
		UUID addressId = addressService.addressCreate(loginId,
				createAddressRequest("Seoul Gangnam Teheran-ro 234", "102-1002", "06235", false)
		).getAddressId();
		
		em.flush();
		em.clear();
		
		String url = "/api/v1/address/" + addressId;
		
		mockMvc
		.perform(delete(url)
				.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isUnauthorized())
		;
		
		UserLoginResponse loginResponse = login();
		
		mockMvc
		.perform(delete(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.data.addressId").value(addressId.toString()))
		.andExpect(jsonPath("$.data.deletedAt").isNotEmpty())
		.andExpect(jsonPath("$.data.isDeleted").value(true))
		;
	}
	
	@Test
	@DisplayName("API: 배송지 한 건 조회 API 테스트")
	void addressInfoOneApiTest() throws Exception {
		AddressCreateRequest request = createAddressRequest("Seoul Gangnam Teheran-ro 123", "101-1001", "06234", true);
		UUID addressId = addressService.addressCreate(loginId, request).getAddressId();
		
		em.flush();
		em.clear();
		
		String url = "/api/v1/address/" + addressId;
		
		mockMvc
		.perform(get(url)
				.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isUnauthorized())
		;
		
		UserLoginResponse loginResponse = login();
		
		mockMvc
		.perform(get(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.data.addressId").value(addressId.toString()))
		.andExpect(jsonPath("$.data.roadAddress").value(request.getRoadAddress()))
		.andExpect(jsonPath("$.data.detailAddress").value(request.getDetailAddress()))
		.andExpect(jsonPath("$.data.zipcode").value(request.getZipcode()))
		.andExpect(jsonPath("$.data.isDefault").value(true))
		;
	}
	
	@Test
	@DisplayName("API: 배송지 목록 조회 API 테스트")
	void addressInfoAllApiTest() throws Exception {
		addressService.addressCreate(loginId,
				createAddressRequest("Seoul Gangnam Teheran-ro 123", "101-1001", "06234", true)
		);
		addressService.addressCreate(loginId,
				createAddressRequest("Seoul Gangnam Teheran-ro 234", "102-1002", "06235", false)
		);
		
		em.flush();
		em.clear();
		
		String url = "/api/v1/address";
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("page", "0");
		params.add("size", "10");
		
		mockMvc
		.perform(get(url)
				.contentType(MediaType.APPLICATION_JSON)
				.params(params)
		)
		.andExpect(status().isUnauthorized())
		;
		
		UserLoginResponse loginResponse = login();
		
		mockMvc
		.perform(get(url)
				.cookie(new Cookie("Authorization", loginResponse.getAccessToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.params(params)
		)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.success").value(true))
		.andExpect(jsonPath("$.data.content", hasSize(2)))
		.andExpect(jsonPath("$.data.page").value(0))
		.andExpect(jsonPath("$.data.size").value(10))
		.andExpect(jsonPath("$.data.totalElements").value(2))
		.andExpect(jsonPath("$.data.totalPages").value(1))
		;
	}
	
	private UserLoginResponse login() {
		UserLoginRequest userLoginRequest = new UserLoginRequest(
				userSignupRequest.getLoginId(),
				userSignupRequest.getPassword()
		);
		
		return userService.login(userLoginRequest);
	}
	
	private AddressCreateRequest createAddressRequest(String roadAddress, String detailAddress, String zipcode, Boolean isDefault) {
		return new AddressCreateRequest(
				roadAddress,
				detailAddress,
				zipcode,
				isDefault
		);
	}
	
}
