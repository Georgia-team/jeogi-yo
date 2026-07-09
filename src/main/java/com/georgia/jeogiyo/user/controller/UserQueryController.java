package com.georgia.jeogiyo.user.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.user.dto.request.UserSearchRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserQueryController {

	private final UserFinderService userFinderService;
	
	// 내 정보 조회 API
	@GetMapping("/me")
	public ResponseEntity<UserInfoResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		
		User user = userFinderService.getUserByLoginId(userDetails.getUsername());
		
		UserInfoResponse response = UserInfoResponse.of(user);
		
		return ResponseEntity.ok(response);
	}
	
	// 유저 목록 검색 API
	// 마스터 권한
	@GetMapping("")
	public ResponseEntity<List<UserInfoResponse>> masterGetUserList(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute UserSearchRequest userSearchRequest
	) {
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		String masterLoginId = userDetails.getUsername();
		
		List<UserInfoResponse> response = userFinderService.getUserList(masterLoginId, userSearchRequest);
		
		return ResponseEntity.ok(response);
	}
	
}
