package com.georgia.jeogiyo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.global.jwt.JwtUtil;
import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserLoginResponse;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController {
	
	private final UserService userCommandService;
	
	private final JwtUtil jwtUtil;
	
	@PostMapping("/signup")
	public ResponseEntity<UserSignupResponse> signup(@RequestBody UserSignupRequest userSignup) {
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		UserSignupResponse signupResponse = userCommandService.signup(userSignup);
		
		return ResponseEntity.ok(signupResponse);
	}
	
	/*
	 * JwtAuthenticationFilter 에 구현되어 있길래 주석처리
	@PostMapping("/login")
	public ResponseEntity<UserLoginResponse> login(
			@RequestBody UserLoginRequest userLogin,
			HttpServletResponse response
	) {
		UserLoginResponse loginResponse = userCommandService.login(userLogin);
		
		jwtUtil.addJwtToCookie(loginResponse.getAccessToken(), response);
		
		return ResponseEntity.ok(loginResponse);
	}
	*/
	
}
