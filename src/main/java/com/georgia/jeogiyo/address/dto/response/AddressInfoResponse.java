package com.georgia.jeogiyo.address.dto.response;

import java.util.UUID;

import com.georgia.jeogiyo.address.entity.Address;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressInfoResponse {

	private final UUID addressId;
	
	private final String roadAddress;
	
	private final String detailAddress;
	
	private final String zipcode;
	
	private final Boolean isDefault;
	
	public static AddressInfoResponse of(Address address) {
		return new AddressInfoResponse(
				address.getAddressId(),
				address.getRoadAddress(),
				address.getDetailAddress(),
				address.getZipcode(),
				address.isDefault()
		);
	}
}
