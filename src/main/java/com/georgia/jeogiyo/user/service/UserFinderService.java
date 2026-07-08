package com.georgia.jeogiyo.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFinderService implements UserFinder {
	
	private final UserRepository userRepository;
	
	@Override
	public User getUserById(String userId) {
		// TODO: 공통 예외 클래스 완료 시 수정
		return userRepository.findById(userId)
				.orElseThrow();
	}

	@Override
	public User getUserByLoginId(String loginId) {
		// TODO: 공통 예외 클래스 완료 시 수정
		return userRepository.findByLoginId(loginId)
				.orElseThrow();
	}

	public List<User> getUserList() {
		// 1. DB에서 마스터 권한 검증
		
		// 2. 페이지네이션
		
		return null;
	}
	
}
