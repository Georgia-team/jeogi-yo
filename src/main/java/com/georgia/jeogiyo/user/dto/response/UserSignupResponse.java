package com.georgia.jeogiyo.user.dto.response;

import java.util.UUID;

import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserSignupResponse {

	private final UUID userId;
	
	private final String email;
	
	private final String loginId;
	
	private final String nickname;
	
	private final Role role;
	
	private final boolean isDeleted;
	
	public static UserSignupResponse of(User user) {
		return new UserSignupResponse(
				user.getUserId(),
				user.getEmail(),
				user.getLoginId(),
				user.getNickname(),
				user.getRole(),
				user.isDeleted()
		);
	}
	
}
