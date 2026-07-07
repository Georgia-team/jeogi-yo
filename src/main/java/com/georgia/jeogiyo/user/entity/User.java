package com.georgia.jeogiyo.user.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(
		name = "id",
		column = @Column(name = "user_id")
)
public class User {

	@Column(name = "login_id", nullable = false, unique = true, length = 10, updatable = false)
	private String loginId;
	
	@Column(name = "nickname", nullable = false, unique = true, length = 20)
	private String nickname;
	
	@Column(name = "phone", nullable = false, length = 20)
	private String phone;
	
	@Column(name = "email", nullable = false, unique = true, length = 255)
	private String email;
	
	@Column(name = "password", nullable = false)
	private String password;
	
	@Column(name = "role", nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role;
	
}
