package com.georgia.jeogiyo.address.service;

import java.util.Optional;
import java.util.UUID;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.user.entity.User;

public interface AddressFinder {

	Address findByUserAndAddressId(User user, UUID addressId);
	
	Optional<Address> findByUserAndDefault(User user);
	
	Optional<Address> findFirstByUserOrderByCreatedAtDesc(User user);
}
