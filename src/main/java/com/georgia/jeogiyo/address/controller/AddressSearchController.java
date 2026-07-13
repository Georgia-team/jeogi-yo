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

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/address")
public class AddressSearchController {

	private final AddressFinderService addressFinder;
	
	@GetMapping("/{addressId}")
	public ResponseEntity<AddressInfoResponse> addressInfoOne(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable String addressId) {
		
		String loginId = userDetails.getUsername();
		
		AddressInfoResponse response = addressFinder.getAddressInfoOne(loginId, addressId);
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("")
	public ResponseEntity<Page<AddressInfoResponse>> addressInfoAll(
			@AuthenticationPrincipal UserDetails userDetails,
			@ModelAttribute AddressSearchRequest addressSearch) {
		
		String loginId = userDetails.getUsername();
		
		Page<AddressInfoResponse> response = addressFinder.getAddressInfoAll(loginId, addressSearch.toPageable("createdAt"));
		
		return ResponseEntity.ok(response);
	}
	
}
