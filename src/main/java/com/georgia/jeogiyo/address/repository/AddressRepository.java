package com.georgia.jeogiyo.address.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.user.entity.User;

public interface AddressRepository extends JpaRepository<Address, UUID> {

	Optional<Address> findByUserAndAddressId(User user, UUID addressId);
	
	Optional<Address> findByUserAndIsDefault(User user, boolean isDefault);
	
	Optional<Address> findFirstByUserOrderByCreatedAtDesc(User user);

	Optional<Address> findFirstByUserAndIsDefaultOrderByCreatedAtDesc(User user, boolean isDefault);
	
	Page<Address> findByUser(User user, Pageable pageable);
}
