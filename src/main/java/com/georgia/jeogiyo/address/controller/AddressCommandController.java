package com.georgia.jeogiyo.address.controller;

import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/address")
public class AddressCommandController {

	private final AddressService addressService;
	
	@PostMapping("")
	public ResponseEntity<AddressCreateResponse> addressCreate(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody AddressCreateRequest addressCreate) {
		
		String loginId = userDetails.getUsername();
		
		AddressCreateResponse response = addressService.addressCreate(loginId, addressCreate);
		
		return ResponseEntity.ok(response);
	}
	
	@PatchMapping("/{addressId}")
	public ResponseEntity<AddressUpdateResponse> addressUpdate(
			@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody AddressUpdateRequest addressUpdate,
			@PathVariable String addressId) {
		
		String loginId = userDetails.getUsername();
		
		AddressUpdateResponse response = addressService.addressUpdate(loginId, addressId, addressUpdate);
		
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/{addressId}")
	public ResponseEntity<AddressDeleteResponse> addressDelete(
			@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable String addressId) {
		
		String loginId = userDetails.getUsername();
		
		AddressDeleteResponse response = addressService.addressDelete(loginId, addressId);
		
		return ResponseEntity.ok(response);
	}
	
}
