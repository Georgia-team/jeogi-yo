package com.georgia.jeogiyo.user.entity;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.georgia.jeogiyo.global.entity.BaseEntity;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class User extends BaseEntity {

	@Id
	@Column(name = "user_id")
	@GeneratedValue(strategy = GenerationType.UUID)
	private String userId;
	
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
	
	public static User create(UserSignupRequest userCreate, PasswordEncoder passwordEncoder) {
		User user = new User();
		
		user.loginId = userCreate.getLoginId();
		user.nickname = userCreate.getNickname();
		user.phone = userCreate.getPhone();
		user.email = userCreate.getEmail();
		user.password = passwordEncoder.encode(userCreate.getPassword());
		
		user.role = Role.CUSTOMER;
		
		return user;
	}
	
	public void changeNickname(String nickname) {
		this.nickname = Objects.requireNonNull(nickname);
	}
	
	public void changePhone(String phone) {
		this.phone = Objects.requireNonNull(phone);
	}
	
	public void changeEmail(String email) {
		this.email = Objects.requireNonNull(email);
	}
	
	public void changePassword(String password, PasswordEncoder passwordEncoder) {
		this.password = passwordEncoder.encode(Objects.requireNonNull(password));
	}
	
	public void changeRole(Role role) {
		this.role = Objects.requireNonNull(role);
	}
	
}
