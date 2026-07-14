package com.georgia.jeogiyo.address.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.request.AddressUpdateRequest;
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
public class AddressCommandFailureTest {

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

	private User user;

	@BeforeEach
	void setUp() {
		UserSignupRequest userSignupRequest = UserFix.getUserSignupRequest();

		user = userFinder.getUserById(userCommandService.signup(userSignupRequest, Role.CUSTOMER).getUserId());
	}

	@Test
	@DisplayName("service-fail: 배송지 삭제 실패 테스트: 유일한 기본 배송지 삭제")
	void failDeleteOnlyDefaultAddressTest() {
		String loginId = user.getLoginId();

		UUID addressId = addressService.addressCreate(loginId, new AddressCreateRequest(
				"서울특별시 종로구 새문안로 123",
				"101-1001",
				"06234",
				true
		)).getAddressId();

		em.flush();
		em.clear();

		assertThatThrownBy(() -> addressService.addressDelete(loginId, addressId.toString()))
		.isInstanceOf(ResponseStatusException.class);

		em.flush();
		em.clear();

		Address address = addressFinder.findByUserAndAddressId(user, addressId);

		assertThat(address.isDeleted()).isFalse();
		assertThat(address.getDeletedAt()).isNull();
		assertThat(address.getDeletedBy()).isNull();
		assertThat(address.isDefault()).isTrue();
	}

	@Test
	@DisplayName("service-fail: 배송지 수정 실패 테스트: 다른 회원의 배송지 수정")
	void failUpdateOtherUserAddressTest() {
		String loginId = user.getLoginId();

		UUID addressId = addressService.addressCreate(loginId, new AddressCreateRequest(
				"서울특별시 종로구 새문안로 123",
				"101-1001",
				"06234",
				true
		)).getAddressId();

		UserSignupRequest otherSignupRequest = new UserSignupRequest(
				"other01",
				"Password01@",
				"otherNickname",
				"02-000-0001",
				"other@email.com"
		);
		String otherLoginId = userCommandService.signup(otherSignupRequest, Role.CUSTOMER).getLoginId();

		em.flush();
		em.clear();

		AddressUpdateRequest updateRequest = new AddressUpdateRequest(
				"서울특별시 종로구 새문안로 123",
				"999-9999",
				"06299",
				false
		);

		assertThatThrownBy(() -> addressService.addressUpdate(otherLoginId, addressId.toString(), updateRequest))
		.isInstanceOf(ResponseStatusException.class);
	}
}