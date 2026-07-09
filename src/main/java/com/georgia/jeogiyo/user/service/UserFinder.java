package com.georgia.jeogiyo.user.service;

import java.util.UUID;

import com.georgia.jeogiyo.user.entity.User;

/**
 * 유저 조회 기능 필요시 UserFinder Interface 주입 받아서 사용.
 * 
 */
public interface UserFinder {
	
	/**
	 * 구현체에서 존재하지 않는 userId 일 시 런타임 예외 발생
	 * 
	 * @param userId
	 * @return User Entity
	 */
	User getUserById(UUID userId);
	
	/**
	 * 구현체에서 존재하지 않는 loginId 일 시 런타임 예외 발생
	 * 
	 * @param loginId
	 * @return User Entity
	 */
	User getUserByLoginId(String loginId);
	
	/**
	 * loginId로 Master 권한 검증과 동시에 User Entity 를 반환합니다.
	 * 
	 * Master 권한이 아닐 경우 throws UserDomainException 이 발생합니다.
	 * Error Message "해당 요청에 대한 권한이 없습니다."
	 * 
	 * @param loginId
	 * @return User Entity
	 */
	User getMasterUserByLoginId(String loginId);
}
