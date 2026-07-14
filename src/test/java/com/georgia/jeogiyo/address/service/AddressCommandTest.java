package com.georgia.jeogiyo.address.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

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
import com.georgia.jeogiyo.user.entity.Role;
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
		
		user = userFinder.getUserById(userCommandService.signup(userSignupRequest, Role.CUSTOMER).getUserId());
	}
	
	@Test
	@DisplayName("service: 배송지 등록 테스트")
	void createAddressTest() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest request = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
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
		
		assertThat(addressEntity.getCreatedBy()).isEqualTo("GUEST");
	}
	
	@Test
	@DisplayName("service: 배송지 수정 테스트")
	void updatedAddressTest() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest request = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101동 1001호",
				"06234",
				true
		);
		AddressCreateResponse address = addressService.addressCreate(loginId, request);
		Address beforeAddress = addressFinder.findByUserAndAddressId(user, address.getAddressId());
		
		em.flush();
		em.clear();
		
		AddressUpdateRequest updateRequest = new AddressUpdateRequest(
				"서울특별시 종로구 종로 1길 234",
				"102동 1002호",
				"06234",
				true
		);
		
		AddressUpdateResponse updateResponse = addressService.addressUpdate(loginId, beforeAddress.getAddressId().toString(), updateRequest);
		
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
	
	@Test
	@DisplayName("service: 배송지 생성 테스트 기본 배송지 전환 케이스")
	void createAddressTest_isDefaultCase() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest addressRequest1 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101동 1001호",
				"06234",
				true
		);
		
		UUID address1ID = addressService.addressCreate(loginId, addressRequest1).getAddressId();
		
		em.flush();
		em.clear();
		
		AddressCreateRequest addressRequest2 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102동 1002호",
				"06234",
				true
		);
		
		UUID address2ID = addressService.addressCreate(loginId, addressRequest2).getAddressId();
		
		em.flush();
		em.clear();
		
		Address address1 = addressFinder.findByUserAndAddressId(user, address1ID);
		Address address2 = addressFinder.findByUserAndAddressId(user, address2ID);
		
		assertThat(address1.isDefault()).isFalse();
		assertThat(address2.isDefault()).isTrue();
	}
	
	@Test
	@DisplayName("service: 배송지 수정 테스트 기본 배송지 전환 케이스")
	void updateAddressTest_isDefaultCase() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest addressCreateReq1 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101동 1001호",
				"06234",
				true
		);
		
		UUID address1ID = addressService.addressCreate(loginId, addressCreateReq1).getAddressId();
		
		AddressCreateRequest addressCreateReq2 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102동 1002호",
				"06234",
				true
		);
		
		UUID address2ID = addressService.addressCreate(loginId, addressCreateReq2).getAddressId();
		
		AddressCreateRequest addressCreateReq3 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102동 1002호",
				"06234",
				true
		);
		
		UUID address3ID = addressService.addressCreate(loginId, addressCreateReq3).getAddressId();
		
		em.flush();
		em.clear();
		
		// address1 isDefault false
		// address2 isDefault true
		// address1 을 다시 isDefault true 요청
		
		Address beforeAddress1 = addressFinder.findByUserAndAddressId(user, address1ID);
		Address beforeAddress2 = addressFinder.findByUserAndAddressId(user, address2ID);
		Address beforeAddress3 = addressFinder.findByUserAndAddressId(user, address3ID);
		
		assertThat(beforeAddress1.isDefault()).isFalse();
		assertThat(beforeAddress2.isDefault()).isFalse();
		assertThat(beforeAddress3.isDefault()).isTrue();
		
		AddressUpdateRequest addressUpdateReq = new AddressUpdateRequest(null, null, null, true);
		
		addressService.addressUpdate(loginId, address1ID.toString(), addressUpdateReq);
		
		em.flush();
		em.clear();
		
		Address address1 = addressFinder.findByUserAndAddressId(user, address1ID);
		Address address2 = addressFinder.findByUserAndAddressId(user, address2ID);
		Address address3 = addressFinder.findByUserAndAddressId(user, address3ID);
		
		assertThat(address1.isDefault()).isTrue();
		assertThat(address1.getRoadAddress()).isEqualTo(addressCreateReq1.getRoadAddress());
		assertThat(address1.getDetailAddress()).isEqualTo(addressCreateReq1.getDetailAddress());
		assertThat(address1.getZipcode()).isEqualTo(addressCreateReq1.getZipcode());
		assertThat(address2.isDefault()).isFalse();
		assertThat(address3.isDefault()).isFalse();
	}
	
	@Test
	@DisplayName("service: 배송지 삭제 테스트")
	void deleteAddressTest() {
		String loginId = user.getLoginId();
		
		AddressCreateRequest addressCreateReq1 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101동 1001호",
				"06234",
				true
		);
		
		UUID address1ID = addressService.addressCreate(loginId, addressCreateReq1).getAddressId();
		
		AddressCreateRequest addressCreateReq2 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102동 1002호",
				"06234",
				true
		);
		
		UUID address2ID = addressService.addressCreate(loginId, addressCreateReq2).getAddressId();
		
		AddressCreateRequest addressCreateReq3 = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102동 1002호",
				"06234",
				true
		);
		
		UUID address3ID = addressService.addressCreate(loginId, addressCreateReq3).getAddressId();
		
		em.flush();
		em.clear();
		
		addressService.addressDelete(loginId, address3ID.toString());
		
		em.flush();
		em.clear();
		
		// address3 이후 최신에 등록된 address2 가 isDefault True 로 변경될 것.
		Address address1 = addressFinder.findByUserAndAddressId(user, address1ID);
		Address address2 = addressFinder.findByUserAndAddressId(user, address2ID);
		assertThat(address1.isDefault()).isFalse();
		assertThat(address2.isDefault()).isTrue();
	}
	
}
