package com.georgia.jeogiyo.user.service;

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
		return userRepository.findById(userId)
				.orElseThrow();
	}

	@Override
	public User getUserByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow();
	}

}
