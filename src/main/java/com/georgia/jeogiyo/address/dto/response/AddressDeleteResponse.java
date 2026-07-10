package com.georgia.jeogiyo.address.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.georgia.jeogiyo.address.entity.Address;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressDeleteResponse {

	private final UUID addressId;
	
	private final LocalDateTime deletedAt;
	
	private final Boolean isDeleted;
	
	public static AddressDeleteResponse of(Address address) {
		return new AddressDeleteResponse(
				address.getAddressId(),
				address.getDeletedAt(),
				address.isDeleted()
		);
	}
}
