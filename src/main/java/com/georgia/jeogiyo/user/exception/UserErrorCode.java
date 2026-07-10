package com.georgia.jeogiyo.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {

	DUPLICATE_EMAIL("이미 사용중인 이메일입니다."),
	DUPLICATE_NICKNAME("이미 사용중인 닉네임입니다."),
	DUPLICATION_LOGIN_ID("이미 사용중인 아이디입니다."),
	
	UPDATE_FAILURE("회원 정보를 수정할 수 없습니다."),
	DELETE_FAILURE("회원탈퇴를 진행할 수 없습니다."),
	
	NOT_FOUND_USER("존재하지 않는 사용자입니다."),
	NOT_AUTHORIZATION("해당 요청에 대한 권한이 없습니다."),
	
	;
	private final String message;
}
