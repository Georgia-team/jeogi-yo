package com.georgia.jeogiyo.user.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserSignupResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "User Signup", description = "회원가입 API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserAuthController {
	
	private final UserService userCommandService;
	
	@Operation(summary = "사용자 회원가입", description = "CUSTOMER 권한으로 회원가입 합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "CUSTOMER 회원가입 성공"),
		@ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
		@ApiResponse(responseCode = "409", description = "이메일 중복, 닉네임 중복")
	})
	@PostMapping("/signup")
	public CommonResponse<UserSignupResponse> signupCustomer(@Valid @RequestBody UserSignupRequest userSignup) {
		UserSignupResponse signupResponse = userCommandService.signup(userSignup, Role.CUSTOMER);
		
		return CommonResponse.success("회원가입 성공", signupResponse);
	}
	
	@Operation(summary = "사장 회원가입", description = "OWNER 권한으로 회원가입 합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "OWNER 회원가입 성공"),
		@ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
		@ApiResponse(responseCode = "409", description = "이메일 중복, 닉네임 중복")
	})
	@PostMapping("/signup/owner")
	public CommonResponse<UserSignupResponse> signupOwner(@Valid @RequestBody UserSignupRequest ownerSignup) {
		UserSignupResponse signupResponse = userCommandService.signup(ownerSignup, Role.OWNER);
		
		return CommonResponse.success("회원가입 성공", signupResponse);
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
