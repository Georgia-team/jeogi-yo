package com.georgia.jeogiyo.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressCreateRequest {

	@NotBlank(message = "주소는 필수 항목입니다.")
	private String roadAddress;
	
	private String detailAddress;
	
	@NotBlank(message = "우편번호는 필수 항목입니다.")
	private String zipcode;
	
	private Boolean isDefault;
}
