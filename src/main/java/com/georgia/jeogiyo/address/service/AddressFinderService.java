package com.georgia.jeogiyo.address.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		return addressRepository.findByUserAndAddressId(user, addressId)
				.orElseThrow(() -> new IllegalArgumentException("회원님의 배송지 정보를 불러올 수 없습니다."));
	}

	@Override
	public Optional<Address> findByUserAndDefault(User user) {
		return addressRepository.findByUserAndIsDefault(user, true);
	}

	@Override
	public Optional<Address> findFirstByUserOrderByCreatedAtDesc(User user) {
		return addressRepository.findFirstByUserAndIsDefaultOrderByCreatedAtDesc(user, false);
	}

	@Override
	public Address findByUserAndAddressId(UUID userId, UUID addressId) {
		User user = userFinder.getUserById(userId);
		
		return addressRepository.findByUserAndAddressId(user, addressId)
				.orElseThrow(() -> new IllegalArgumentException("회원님의 배송지 정보를 불러올 수 없습니다."));
	}
	
	
}
