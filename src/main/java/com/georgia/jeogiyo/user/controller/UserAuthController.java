package com.georgia.jeogiyo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController {
	
	private final UserService userCommandService;
	
	@PostMapping("/signup")
	public ResponseEntity<UserSignupResponse> signupCustomer(@RequestBody UserSignupRequest userSignup) {
		UserSignupResponse signupResponse = userCommandService.signup(userSignup, Role.CUSTOMER);
		
		return ResponseEntity.ok(signupResponse);
	}
	
	@PostMapping("/signup/owner")
	public ResponseEntity<UserSignupResponse> signupOwner(@RequestBody UserSignupRequest ownerSignup) {
		UserSignupResponse signupResponse = userCommandService.signup(ownerSignup, Role.OWNER);
		
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
