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
public class AddressService {

	private final AddressRepository addressRepo;
	
	private final AddressFinder addressFinder;
	
	private final UserFinder userFinder;
	
	// 배송지 등록
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
	public AddressUpdateResponse addressUpdate(String loginId, UUID addressId, AddressUpdateRequest addressUpdate) {
		User user = userFinder.getUserByLoginId(loginId);
		
		Address address = addressFinder.findByUserAndAddressId(user, addressId);
		
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
	public AddressDeleteResponse addressDelete(String loginId, UUID addressId) {
		User user = userFinder.getUserByLoginId(loginId);
		
		Address address = addressFinder.findByUserAndAddressId(user, addressId);
		
		if(address.isDefault()) {
			
		}
		return null;
	}
	
}
