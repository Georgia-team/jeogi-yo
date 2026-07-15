package com.georgia.jeogiyo.address.dto.response;

import java.util.UUID;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressDeleteResponse {

	private final UUID addressId;
	
	private final Boolean isDeleted;
	
	public static AddressDeleteResponse of(Address address) {
		if(address.getAddressId() == null) {
			throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}
		
		return new AddressDeleteResponse(
				address.getAddressId(),
				address.isDeleted()
		);
	}
}
