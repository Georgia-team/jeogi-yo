package com.georgia.jeogiyo.address.entity;

import java.util.UUID;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.global.entity.BaseEntity;
import com.georgia.jeogiyo.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_address")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

	@Id
	@Column(name = "address_id")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID addressId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;
	
	@Column(name = "road_address", nullable = false, length = 100)
	private String roadAddress;
	
	@Column(name = "detail_address", length = 100)
	private String detailAddress;
	
	@Column(name = "zipcode", nullable = false, length = 10)
	private String zipcode;
	
	@Column(name = "is_default", nullable = false)
	private boolean isDefault = false;
	
	public static Address create(User user, AddressCreateRequest addressCreate) {
		Address address = new Address();
		
		address.user = user;
		address.roadAddress = addressCreate.getRoadAddress();
		address.detailAddress = addressCreate.getDetailAddress();
		address.zipcode = addressCreate.getZipcode();
		
		if(addressCreate.getIsDefault() != null) {
			address.isDefault = addressCreate.getIsDefault();
		}
		
		return address;
	}
	
	
	
}
