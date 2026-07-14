package com.georgia.jeogiyo.address.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.address.dto.response.AddressInfoResponse;
import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.address.repository.AddressRepository;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressFinderService implements AddressFinder {

	private final AddressRepository addressRepository;

	private final UserFinder userFinder;
	
	@Override
	public Address findByUserAndAddressId(User user, UUID addressId) {
		return addressRepository.findByUserAndAddressIdAndIsDeletedFalse(user, addressId)
				.orElseThrow(() -> new IllegalArgumentException("회원님의 배송지 정보를 불러올 수 없습니다."));
	}

	@Override
	public Optional<Address> findByUserAndDefault(User user) {
		return addressRepository.findByUserAndIsDefaultTrueAndIsDeletedFalse(user);
	}

	@Override
	public Optional<Address> findFirstByUserOrderByCreatedAtDesc(User user) {
		return addressRepository.findFirstByUserAndIsDefaultTrueAndIsDeletedFalseOrderByCreatedAtDesc(user);
	}

	@Override
	public Address findByUserAndAddressId(UUID userId, UUID addressId) {
		User user = userFinder.getUserById(userId);
		
		return addressRepository.findByUserAndAddressIdAndIsDeletedFalse(user, addressId)
				.orElseThrow(() -> new IllegalArgumentException("회원님의 배송지 정보를 불러올 수 없습니다."));
	}
	
	public AddressInfoResponse getAddressInfoOne(String loginId, String addressId) {
		User user = userFinder.getUserByLoginId(loginId);
		
		Address address = findByUserAndAddressId(user, UUID.fromString(addressId));
		
		return AddressInfoResponse.of(address);
	}
	
	public Page<AddressInfoResponse> getAddressInfoAll(String loginId, Pageable pageable) {
		User user = userFinder.getUserByLoginId(loginId);
		
		Page<Address> address = addressRepository.findByUser(user, pageable);
		
		return address.map(AddressInfoResponse::of);
	}
	
}
