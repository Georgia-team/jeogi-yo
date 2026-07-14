package com.georgia.jeogiyo.address.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.address.dto.request.AddressSearchRequest;
import com.georgia.jeogiyo.address.dto.response.AddressInfoResponse;
import com.georgia.jeogiyo.address.service.AddressFinderService;
import com.georgia.jeogiyo.global.response.CommonResponse;
import com.georgia.jeogiyo.global.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Address", description = "주소 Query API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/address")
public class AddressSearchController {

	private final AddressFinderService addressFinder;
	
	@Operation(summary = "주소 한 건 조회", description = "주소를 한 건 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주소 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
    @ApiResponse(responseCode = "403", description = "권한 없음"),
    @ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음")
	})
	@GetMapping("/{addressId}")
	public CommonResponse<AddressInfoResponse> addressInfoOne(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable String addressId) {
		
		String loginId = userDetails.getUsername();
		
		AddressInfoResponse response = addressFinder.getAddressInfoOne(loginId, addressId);
		
		return CommonResponse.success("주소 조회 성공", response);
	}
	
	@Operation(summary = "주소 목록 조회", description = "주소 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주소 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
    @ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@GetMapping("")
	public CommonResponse<PageResponse<AddressInfoResponse>> addressInfoAll(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute AddressSearchRequest addressSearch) {
		
		String loginId = userDetails.getUsername();
		
		Page<AddressInfoResponse> addressPages = addressFinder.getAddressInfoAll(loginId, addressSearch.toPageable("createdAt"));
		
		PageResponse<AddressInfoResponse> response = PageResponse.from(addressPages, x -> x);
		
		return CommonResponse.success("주소 목록 조회 성공", response);
	}
	
}
