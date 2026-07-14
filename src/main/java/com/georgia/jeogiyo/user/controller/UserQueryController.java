package com.georgia.jeogiyo.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.user.dto.request.UserSearchRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.service.UserFinderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "User", description = "회원 Query API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserQueryController {

	private final UserFinderService userFinderService;
	
	@Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
	})
	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER', 'OWNER') and #userDetails.username == principal.username")
	public ResponseEntity<CommonResponse<UserInfoResponse>> getMe(@AuthenticationPrincipal UserDetails userDetails) {
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		
		User user = userFinderService.getUserByLoginId(userDetails.getUsername());
		
		UserInfoResponse response = UserInfoResponse.of(user);
		
		return ResponseEntity.ok(CommonResponse.success("내 정보 조회 성공", response));
	}
	
	@Operation(summary = "유저 목록 조회", description = "마스터 권한용 유저 목록 조회")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "유저 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@GetMapping("")
	@PreAuthorize("hasAnyRole('MASTER') and #userDetails.username == principal.username")
	public ResponseEntity<CommonResponse<PageResponse<UserInfoResponse>>> masterGetUserList(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @ModelAttribute UserSearchRequest userSearchRequest
	) {
		// TODO: 공통 응답 객체 완료되면 반환 타입 바꿀 예정
		String masterLoginId = userDetails.getUsername();
		
		Page<UserInfoResponse> userPagenation = userFinderService.getUserList(masterLoginId, userSearchRequest);
		
		PageResponse<UserInfoResponse> response = PageResponse.from(userPagenation, x -> x);
		
		return ResponseEntity.ok(CommonResponse.success("유저 목록 조회 성공", response));
	}
	
}
