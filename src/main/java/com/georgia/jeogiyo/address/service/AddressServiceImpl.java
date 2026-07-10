package com.georgia.jeogiyo.address.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.request.AddressUpdateRequest;
import com.georgia.jeogiyo.address.dto.response.AddressCreateResponse;
import com.georgia.jeogiyo.address.dto.response.AddressDeleteResponse;
import com.georgia.jeogiyo.address.dto.response.AddressUpdateResponse;
import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.address.repository.AddressRepository;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

	private final AddressRepository addressRepo;
	
	private final AddressFinder addressFinder;
	
	private final UserFinder userFinder;
	
	// 배송지 등록
	@Override
	public AddressCreateResponse addressCreate(String loginId, AddressCreateRequest addressCreate) {
		User user = userFinder.getUserByLoginId(loginId);
		
		if(addressCreate.getIsDefault() == true) {
			Address defaultAddress = addressFinder.findByUserAndDefault(user)
					.orElse(null);
			
			
			if(defaultAddress != null) {
				defaultAddress.changeNotDefault();
			}
		}
		
		Address newAddress = Address.create(user, addressCreate);
		
		Address saved = addressRepo.save(newAddress);
		
		return AddressCreateResponse.of(saved);
	}
	
	// 배송지 수정
	@Override
	public AddressUpdateResponse addressUpdate(String loginId, String addressId, AddressUpdateRequest addressUpdate) {
		User user = userFinder.getUserByLoginId(loginId);
		
		Address address = addressFinder.findByUserAndAddressId(user, UUID.fromString(addressId));
		
		if(!address.isDefault() && addressUpdate.getIsDefault()) {
			Address defaultAddress = addressFinder.findByUserAndDefault(user)
					.orElse(null);
			
			if(defaultAddress != null) {
				defaultAddress.changeNotDefault();
			}
		}
		
		address.changeAddressInfo(addressUpdate);
		
		return AddressUpdateResponse.of(address);
	}
	
	// 배송지 삭제
	@Override
	public AddressDeleteResponse addressDelete(String loginId, String addressId) {
		User user = userFinder.getUserByLoginId(loginId);
		
		Address address = addressFinder.findByUserAndAddressId(user, UUID.fromString(addressId));
		
		if(address.isDefault()) {
			Address latestAddress = addressFinder.findFirstByUserOrderByCreatedAtDesc(user)
					.orElseThrow(() -> new IllegalStateException("기본 배송지 하나만 있는 경우 삭제 처리가 불가합니다."));
			
			latestAddress.changeDefault();
		}
		
		address.softDelete(user.getLoginId());
		
		return AddressDeleteResponse.of(address);
	}
	
}
