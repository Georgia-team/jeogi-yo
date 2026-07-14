package com.georgia.jeogiyo.user.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.user.dto.request.UserDeleteRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
import com.georgia.jeogiyo.user.dto.response.UserDeleteResponse;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "User", description = "회원 Command API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserCommandController {

	private final UserService userCommandService;
	
	@Operation(summary = "회원 수정", description = "본인의 회원 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "회원 수정 성공"),
		@ApiResponse(responseCode = "400", description = "요청값 검증 실패"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "409", description = "이메일 중복, 닉네임 중복")
	})
	@PatchMapping("/me")
	@Secured({"ROLE_CUSTOMER", "ROLE_OWNER", "ROLE_MASTER"})
	public CommonResponse<UserInfoResponse> updateMe(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody UserUpdateRequest userUpdateRequest
	) {
		String loginId = userDetails.getUsername();
		
		System.out.println("UPDATE_ME_LOGIN_ID: " + loginId);
		
		UserInfoResponse response = userCommandService.update(loginId, userUpdateRequest);
		
		// TODO: 공통 응답 객체 추가시 수정
		return CommonResponse.success("회원 수정 성공", response);
	}
	
	@Operation(summary = "회원 삭제", description = "회원 탈퇴합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "409", description = "이미 탈퇴한 회원")
	})
	@DeleteMapping("/me")
	@Secured({"ROLE_CUSTOMER", "ROLE_OWNER", "ROLE_MASTER"})
	public CommonResponse<UserDeleteResponse> deleteMe(
			@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody UserDeleteRequest userDelete
	) {
		String loginId = userDetails.getUsername();
		
		UserDeleteResponse response = userCommandService.delete(loginId, userDelete);
		
		return CommonResponse.success("회원 탈퇴 성공", response);
	}
	
}
