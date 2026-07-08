package com.georgia.jeogiyo.user.fixture;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;

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
	
	public static UserUpdateRequest getUserUpdateRequest() {
		return new UserUpdateRequest(
				"test0123",
				"02-111-1234",
				"test1234@email.com",
				"password123A@"
		);
	}
	
}
