package com.georgia.jeogiyo.user.exception;

// TODO: 임시로 RuntimeException 상속. 공통 예외 클래스 완료시 바꿀 예정
public class UserDomainException extends RuntimeException {

//	public UserDomainException(String message) {
//		super(message);
//	}
	
	public UserDomainException(UserErrorCode code) {
		super(code.getMessage());
	}
	
}
