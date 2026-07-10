package com.georgia.jeogiyo.user.dto.response;

import java.util.UUID;

import com.georgia.jeogiyo.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDeleteResponse {

	private final UUID userId;
	
	public static UserDeleteResponse of(User user) {
		return new UserDeleteResponse(
				user.getUserId()
		);
	}
	
}
