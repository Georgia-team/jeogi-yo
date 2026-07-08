package com.georgia.jeogiyo.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserCommandController {

	private final UserService userCommandService;
	
	@PatchMapping("/me")
	public ResponseEntity<UserInfoResponse> updateMe(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody UserUpdateRequest userUpdateRequest
	) {
		String userId = userDetails.getUsername();
		
		UserInfoResponse response = userCommandService.update(userId, userUpdateRequest);
		
		// TODO: 공통 응답 객체 추가시 수정
		return ResponseEntity.ok(response);
	}
	
}
