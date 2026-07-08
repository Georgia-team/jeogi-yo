package com.georgia.jeogiyo.user;

import com.georgia.jeogiyo.user.dto.UserSignupRequest;

public class UserFix {
	public static UserSignupRequest getUserCreateRequest() {
		return new UserSignupRequest(
				"test1234",
				"password1234A@",
				"nickname",
				"02-000-0000",
				"test@email.com"
		);
	}
}
