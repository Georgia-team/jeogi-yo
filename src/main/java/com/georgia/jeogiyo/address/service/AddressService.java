package com.georgia.jeogiyo.address.service;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.request.AddressUpdateRequest;
import com.georgia.jeogiyo.address.dto.response.AddressCreateResponse;
import com.georgia.jeogiyo.address.dto.response.AddressDeleteResponse;
import com.georgia.jeogiyo.address.dto.response.AddressUpdateResponse;

public interface AddressService {

	/**
	 * 회원의 새로운 배송지를 등록합니다.
	 * 기본 배송지로 설정하는 경우 이미 기본 배송지로 설정된 배송지는 False 처리됩니다.
	 * 
	 * @param loginId
	 * @param addressCreate
	 * @return AddressCreateResponse DTO
	 */
	AddressCreateResponse addressCreate(String loginId, AddressCreateRequest addressCreate);

	/**
	 * 회원의 배송지를 수정합니다.
	 * 기본 배송지로 설정하는 경우 이미 기본 배송지로 설정된 배송지는 False 처리됩니다.
	 * 
	 * @param loginId
	 * @param addressId
	 * @param addressUpdate
	 * @return AddressUpdateResponse DTO
	 */
	AddressUpdateResponse addressUpdate(String loginId, String addressId, AddressUpdateRequest addressUpdate);

	/**
	 * 회원의 배송지를 삭제합니다.
	 * 기본 배송지를 삭제하는 경우 해당 회원의 배송지 목록에서 최신 등록된 배송지를 기본 배송지로 지정합니다.
	 * 배송지가 하나만 존재하는 경우 삭제가 불가합니다.
	 * 
	 * @param loginId
	 * @param addressId
	 * @return AddressDeleteResponse DTO
	 */
	AddressDeleteResponse addressDelete(String loginId, String addressId);

}
