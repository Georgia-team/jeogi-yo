package com.georgia.jeogiyo.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

	private String nickname;
	
	@Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 연락처 형식이 아닙니다.")
	private String phone;
	
	@Pattern(
	    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", 
	    message = "올바른 이메일 형식이 아닙니다."
	)
	private String email;
	
	@Pattern(
			regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
			message = "비밀번호는 영문 대/소문자, 숫자, 특수문자를 모두 포함해야 합니다."
	)
	private String password;
}
