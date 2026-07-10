package com.georgia.jeogiyo.address.service;

import java.util.Optional;
import java.util.UUID;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.user.entity.User;

public interface AddressFinder {

	/**
	 * User를 조회 후 그 값과 AddressId로 배송지 한 건 조회합니다.
	 * 배송지가 존재하지 않는 경우 IllegalArgumentException 이 발생합니다.
	 * 
	 * @param user
	 * @param addressId
	 * @throws IllegalArgumentException.class "회원님의 배송지 정보를 불러올 수 없습니다."
	 * @return Address Entity
	 */
	Address findByUserAndAddressId(User user, UUID addressId);

	/**
	 * userId와 addressId 로 배송지 한 건 조회합니다.
	 * 회원이 존재하지 않는 경우 UserDomainException.class "존재하지 않는 사용자입니다."
	 * 배송지가 존재하지 않는 경우 IllegalArgumentException 이 발생합니다.
	 * 
	 * @param userId
	 * @param addressId
	 * @return Address Entity
	 */
	Address findByUserAndAddressId(UUID userId, UUID addressId);
	
	/**
	 * User를 조회 후 그 값으로 해당 User의 기본 배송지를 한 건 조회합니다.
	 * 도메인에 따라 예외 처리가 달라질 것을 예상하여 Optional로 반환합니다.
	 * 
	 * @param user
	 * @return Optional Address Entity
	 */
	Optional<Address> findByUserAndDefault(User user);
	
	/**
	 * 기본 배송지가 아닌 해당 User의 배송지 목록 중 가장 최신에 등록된 배송지를 한 건 조회합니다.
	 * 
	 * @param user
	 * @return Optional Address Entity
	 */
	Optional<Address> findFirstByUserOrderByCreatedAtDesc(User user);
}
