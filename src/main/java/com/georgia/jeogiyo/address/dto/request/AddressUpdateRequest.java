package com.georgia.jeogiyo.address.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressUpdateRequest {

	private String roadAddress;
	
	private String detailAddress;
	
	private String zipcode;
	
	private Boolean isDefault;
}
