package com.georgia.jeogiyo.user.dto.response;

import com.georgia.jeogiyo.user.entity.Role;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserLoginResponse {

	private final String accessToken;
	
	private final String userId;
	
	private final String loginId;
	
	private final String nickname;
	
	private final Role role;
}
