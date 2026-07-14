package com.georgia.jeogiyo.address.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUpdateResponse {

	private final UUID addressId;
	
	private final String roadAddress;
	
	private final String detailAddress;
	
	private final String zipcode;
	
	private final Boolean isDefault;
	
	private final LocalDateTime updatedAt;
	
	public static AddressUpdateResponse of(Address address) {
		if(address.getAddressId() == null) {
			throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}
		
		return new AddressUpdateResponse(
				address.getAddressId(),
				address.getRoadAddress(),
				address.getDetailAddress(),
				address.getZipcode(),
				address.isDefault(),
				address.getUpdatedAt()
		);
	}
}
