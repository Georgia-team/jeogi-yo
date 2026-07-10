package com.georgia.jeogiyo.address.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.request.AddressUpdateRequest;
import com.georgia.jeogiyo.address.dto.response.AddressCreateResponse;
import com.georgia.jeogiyo.address.dto.response.AddressUpdateResponse;
import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.service.UserFinder;
import com.georgia.jeogiyo.user.service.UserService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class AddressCommandTest {

	@Autowired
	private UserService userCommandService;
	
	@Autowired
	private UserFinder userFinder;
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private AddressFinder addressFinder;
	
	@Autowired
	private EntityManager em;
	
	private UserSignupRequest userSignupRequest;
	
	private User user;
	
	@BeforeEach
	void setUp() {
		userSignupRequest = UserFix.getUserSignupRequest();
		
		user = userFinder.getUserById(userCommandService.signup(userSignupRequest).getUserId());
	}
	
	@Test
	@DisplayName("service: 배송지 등록 테스트")
	void createAddressTest() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest request = new AddressCreateRequest(
				"서울특별시 강남구 테헤란로 123",
				"101동 1001호",
				"06234",
				true
		);
		
		AddressCreateResponse address = addressService.addressCreate(loginId, request);
		
		assertThat(address.getAddressId()).isNotNull();
		assertThat(address.getRoadAddress()).isEqualTo(request.getRoadAddress());
		assertThat(address.getDetailAddress()).isEqualTo(request.getDetailAddress());
		assertThat(address.getZipcode()).isEqualTo(request.getZipcode());
		assertThat(address.getIsDefault()).isTrue();
		assertThat(address.getCreatedAt()).isNotNull();
		
		em.flush();
		em.clear();
		
		Address addressEntity = addressFinder.findByUserAndAddressId(user, address.getAddressId());
		
		assertThat(addressEntity.getCreatedBy()).isEqualTo("user");
	}
	
	@Test
	@DisplayName("service: 배송지 수정 테스트")
	void updatedAddressTest() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest request = new AddressCreateRequest(
				"서울특별시 강남구 테헤란로 123",
				"101동 1001호",
				"06234",
				true
		);
		AddressCreateResponse address = addressService.addressCreate(loginId, request);
		Address beforeAddress = addressFinder.findByUserAndAddressId(user, address.getAddressId());
		
		em.flush();
		em.clear();
		
		AddressUpdateRequest updateRequest = new AddressUpdateRequest(
				"서울특별시 강남구 테헤란로 234",
				"102동 1002호",
				"06234",
				true
		);
		
		AddressUpdateResponse updateResponse = addressService.addressUpdate(loginId, beforeAddress.getAddressId(), updateRequest);
		
		em.flush();
		em.clear();
		
		Address afterAddress = addressFinder.findByUserAndAddressId(user, address.getAddressId());
		
		assertThat(beforeAddress.getAddressId()).isEqualTo(afterAddress.getAddressId());
		assertThat(beforeAddress.getRoadAddress()).isNotEqualTo(afterAddress.getRoadAddress());
		assertThat(beforeAddress.getDetailAddress()).isNotEqualTo(afterAddress.getDetailAddress());
		assertThat(beforeAddress.getZipcode()).isEqualTo(afterAddress.getZipcode());
		assertThat(afterAddress.isDefault()).isTrue();
		
		assertThat(afterAddress.getRoadAddress()).isEqualTo(updateRequest.getRoadAddress());
		assertThat(afterAddress.getDetailAddress()).isEqualTo(updateRequest.getDetailAddress());
		assertThat(afterAddress.getZipcode()).isEqualTo(updateRequest.getZipcode());
		
		assertThat(updateRequest.getRoadAddress()).isEqualTo(updateResponse.getRoadAddress());
		assertThat(updateRequest.getDetailAddress()).isEqualTo(updateResponse.getDetailAddress());
		assertThat(updateRequest.getZipcode()).isEqualTo(updateResponse.getZipcode());
		assertThat(updateResponse.getIsDefault()).isTrue();
	}
	
}
