package com.georgia.jeogiyo.user.service;

import org.springframework.stereotype.Service;

import com.georgia.jeogiyo.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {

	private final UserRepository userRepository;
	
	
}
