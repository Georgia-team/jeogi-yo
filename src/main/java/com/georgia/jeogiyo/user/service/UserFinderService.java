package com.georgia.jeogiyo.user.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.user.dto.request.UserSearchRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.exception.UserDomainException;
import com.georgia.jeogiyo.user.exception.UserErrorCode;
import com.georgia.jeogiyo.user.repository.UserQueryDslRepository;
import com.georgia.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFinderService implements UserFinder {
	
	private final UserRepository userRepository;
	
	private final UserQueryDslRepository userDsl;
	
	@Override
	public User getUserById(UUID userId) {
		// TODO: 공통 예외 클래스 완료 시 수정
		return userRepository.findById(userId)
				// TODO: 존재하지 않는 사용자입니다.
				.orElseThrow(() -> new UserDomainException(UserErrorCode.NOT_FOUND_USER));
	}

	@Override
	public User getUserByLoginId(String loginId) {
		// TODO: 공통 예외 클래스 완료 시 수정
		return userRepository.findByLoginIdAndIsDeleted(loginId, false)
				// TODO: 존재하지 않는 사용자입니다.
				.orElseThrow(() -> new UserDomainException(UserErrorCode.NOT_FOUND_USER));
	}

	@Override
	public User getMasterUserByLoginId(String loginId) {
		// TODO: 공통 예외 클래스 완료 시 수정
		User masterUser = userRepository.findByLoginId(loginId)
				// TODO: 존재하지 않는 사용자입니다.
				.orElseThrow();
		
		if(!masterUser.isMaster()) {
			// TODO: 해당 요청에 대한 권한이 없습니다.
			throw new UserDomainException(UserErrorCode.NOT_AUTHORIZATION);
		}
		
		return masterUser;
	}
	
	@Override
	public User getOwnerUserByLoginId(String loginId) {
		// TODO: 공통 예외 클래스 완료 시 수정
		User ownerUser = userRepository.findByLoginId(loginId)
				// TODO: 존재하지 않는 사용자입니다.
				.orElseThrow();
		
		if(!ownerUser.isOwner()) {
			// TODO: 해당 요청에 대한 권한이 없습니다.
			throw new UserDomainException(UserErrorCode.NOT_AUTHORIZATION);
		}
		
		return ownerUser;
	}
	
	public Page<UserInfoResponse> getUserList(String masterLoginId, UserSearchRequest userSearch) {
		// 1. DB에서 마스터 권한 검증
		getMasterUserByLoginId(masterLoginId);
		
		// 2. QueryDSL
		return userDsl.findByRoleAndKeyword(userSearch);
	}
	
}
