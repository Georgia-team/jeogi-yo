package com.georgia.jeogiyo.user.dto.response;

import java.time.LocalDateTime;

import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoResponse {

	private final String userId;
	
	private final String loginId;
	
	private final String nickname;
	
	private final String phone;
	
	private final String email;
	
	private final Role role;
	
	private final LocalDateTime createdAt;
	
	public static UserInfoResponse of(User user) {
		return new UserInfoResponse(
				user.getUserId(),
				user.getLoginId(),
				user.getNickname(),
				user.getPhone(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt()
		);
	}
}
