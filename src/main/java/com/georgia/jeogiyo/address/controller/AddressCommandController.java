package com.georgia.jeogiyo.address.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.dto.request.AddressUpdateRequest;
import com.georgia.jeogiyo.address.dto.response.AddressCreateResponse;
import com.georgia.jeogiyo.address.dto.response.AddressDeleteResponse;
import com.georgia.jeogiyo.address.dto.response.AddressUpdateResponse;
import com.georgia.jeogiyo.address.service.AddressService;
import com.georgia.jeogiyo.global.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Tag(name = "Address", description = "주소 Command API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/address")
public class AddressCommandController {

	private final AddressService addressService;
	
	@Operation(summary = "주소 등록", description = "유저 본인의 주소를 등록합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주소 등록 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@PostMapping("")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER', 'OWNER') and #userDetails.username == principal.username")
	public CommonResponse<AddressCreateResponse> addressCreate(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody AddressCreateRequest addressCreate) {
		
		String loginId = userDetails.getUsername();
		
		AddressCreateResponse response = addressService.addressCreate(loginId, addressCreate);
		
		return CommonResponse.success("주소 등록 성공", response);
	}
	
	@Operation(summary = "주소 수정", description = "유저 본인이 등록한 주소를 수정합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주소 수정 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음")
	})
	@PatchMapping("/{addressId}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER', 'OWNER') and #userDetails.username == principal.username")
	public CommonResponse<AddressUpdateResponse> addressUpdate(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody AddressUpdateRequest addressUpdate,
			@PathVariable String addressId) {
		
		String loginId = userDetails.getUsername();
		
		AddressUpdateResponse response = addressService.addressUpdate(loginId, addressId, addressUpdate);
		
		return CommonResponse.success("주소 수정 성공", response);
	}
	
	@Operation(summary = "주소 삭제", description = "유저 본인이 등록한 주소를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "주소 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음"),
		@ApiResponse(responseCode = "409", description = "마지막 배송지는 삭제할 수 없음")
	})
	@DeleteMapping("/{addressId}")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER', 'OWNER') and #userDetails.username == principal.username")
	public CommonResponse<AddressDeleteResponse> addressDelete(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable String addressId) {
		
		String loginId = userDetails.getUsername();
		
		AddressDeleteResponse response = addressService.addressDelete(loginId, addressId);
		
		return CommonResponse.success("주소 삭제 성공", response);
	}
	
}
