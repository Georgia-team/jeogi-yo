package com.georgia.jeogiyo.user.dto;

import java.time.LocalDateTime;

import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignupResponse {

	private String userId;
	
	private String loginId;
	
	private String nickname;
	
	private Role role;
	
	private LocalDateTime createdAt;
	
	private boolean isDeleted;
	
	public static UserSignupResponse of(User user) {
		return new UserSignupResponse(
				user.getUserId(),
				user.getLoginId(),
				user.getNickname(),
				user.getRole(),
				user.getCreatedAt(),
				user.isDeleted()
		);
	}
	
}
