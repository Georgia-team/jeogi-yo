package com.georgia.jeogiyo.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.georgia.jeogiyo.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDeleteResponse {

	private UUID userId;
	
	private LocalDateTime deletedAt;
	
	public static UserDeleteResponse of(User user) {
		return new UserDeleteResponse(
				user.getUserId(),
				user.getDeletedAt()
		);
	}
	
}
