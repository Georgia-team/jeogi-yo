package com.georgia.jeogiyo.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.response.AddressInfoResponse;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.service.UserFinder;
import com.georgia.jeogiyo.user.service.UserService;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class AddressSearchTest {

	@Autowired
	private UserService userCommandService;

	@Autowired
	private UserFinder userFinder;

	@Autowired
	private AddressService addressService;

	@Autowired
	private AddressFinder addressFinder;

	@Autowired
	private AddressFinderService addressFinderService;

	@Autowired
	private EntityManager em;

	private User user;

	@BeforeEach
	void setUp() {
		UserSignupRequest userSignupRequest = UserFix.getUserSignupRequest();

		user = userFinder.getUserById(userCommandService.signup(userSignupRequest, Role.CUSTOMER).getUserId());
	}

	@Test
	@DisplayName("service-search: 배송지 단건 조회 테스트")
	void getAddressInfoOneTest() {
		String loginId = user.getLoginId();

		AddressCreateRequest request = new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101-1001",
				"06234",
				true
		);

		UUID addressId = addressService.addressCreate(loginId, request).getAddressId();

		em.flush();
		em.clear();

		AddressInfoResponse response = addressFinderService.getAddressInfoOne(loginId, addressId.toString());

		assertThat(response.getAddressId()).isEqualTo(addressId);
		assertThat(response.getRoadAddress()).isEqualTo(request.getRoadAddress());
		assertThat(response.getDetailAddress()).isEqualTo(request.getDetailAddress());
		assertThat(response.getZipcode()).isEqualTo(request.getZipcode());
		assertThat(response.getIsDefault()).isTrue();
	}

	@Test
	@DisplayName("service-search: 배송지 목록 조회 테스트: 삭제된 배송지 제외")
	void getAddressInfoAllTest_ExcludeDeleted() {
		String loginId = user.getLoginId();

		UUID address1Id = addressService.addressCreate(loginId, new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101-1001",
				"06234",
				true
		)).getAddressId();

		UUID address2Id = addressService.addressCreate(loginId, new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102-1002",
				"06235",
				true
		)).getAddressId();

		addressService.addressDelete(loginId, address2Id.toString());

		em.flush();
		em.clear();

		Page<AddressInfoResponse> response = addressFinderService.getAddressInfoAll(loginId, PageRequest.of(0, 10));

		assertThat(response).hasSize(1);
		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().get(0).getAddressId()).isEqualTo(address1Id);
		assertThat(response.getContent().get(0).getIsDefault()).isTrue();
	}

	@Test
	@DisplayName("service-fail: 배송지 조회 실패 테스트: 삭제된 배송지 조회")
	void failGetAddressInfoOneTest_DeletedAddress() {
		String loginId = user.getLoginId();

		UUID address1Id = addressService.addressCreate(loginId, new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"101-1001",
				"06234",
				true
		)).getAddressId();

		UUID address2Id = addressService.addressCreate(loginId, new AddressCreateRequest(
				"서울특별시 종로구 종로 1길 123",
				"102-1002",
				"06235",
				true
		)).getAddressId();

		addressService.addressDelete(loginId, address2Id.toString());

		em.flush();
		em.clear();

		assertThatThrownBy(() -> addressFinder.findByUserAndAddressId(user, address2Id))
		.isInstanceOf(BusinessException.class)
		.hasMessage(GlobalErrorCode.NOT_FOUND_ADDRESS.getMessage());

		assertThat(addressFinder.findByUserAndAddressId(user, address1Id).isDefault()).isTrue();
	}
}