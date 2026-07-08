package com.georgia.jeogiyo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.user.dto.response.UserLoginResponse;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.service.UserCommandService;
import com.georgia.jeogiyo.user.service.UserFinder;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController {
	
	private final UserCommandService userCommandService;
	
	private final UserFinder userFinder;
	
	@PostMapping("/signup")
	public ResponseEntity<UserSignupResponse> signup() {
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		return null;
	}
	
	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> login() {
		// TODO: 인증 엔티티 완료시 개발 시작
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		
		return null;
	}
	
}
