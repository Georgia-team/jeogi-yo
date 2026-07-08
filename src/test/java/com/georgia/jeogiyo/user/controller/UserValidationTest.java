package com.georgia.jeogiyo.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class UserValidationTest {

	private static Validator validator;
	
	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	@Test
	@DisplayName("validate: 올바른 회원가입 요청 테스트")
	void signUpRequestSuccess() {
		UserSignupRequest UserSignupRequest = new UserSignupRequest(
				"test1234",
				"password1234A@",
				"nickname",
				"02-000-0000",
				"test@email.com"
		);
		
		Set<ConstraintViolation<UserSignupRequest>> violations = validator.validate(UserSignupRequest);
		
		assertThat(violations).isEmpty();
	}
	
	@Test
	@DisplayName("validate: (Size) 잘못된 회원가입 요청 테스트")
	void signUpRequestFailure_Size() {
		// Validation Size Test
		UserSignupRequest sizeTestRequest = new UserSignupRequest(
				"test123456787987980560646",
				"password1234A@1234567897898",
				"nickname",
				"02-000-0000",
				"test@email.com"
		);
		
		Set<ConstraintViolation<UserSignupRequest>> sizeViolations = validator.validate(sizeTestRequest);
		
		assertThat(sizeViolations).isNotEmpty();
		
		List<String> sizeErrorFields = sizeViolations.stream()
				.map(v -> v.getPropertyPath().toString())
				.toList();
		
		assertThat(sizeErrorFields).containsExactlyInAnyOrder(
				"loginId", "password"
		);
		
	}
	
	@Test
	@DisplayName("validate: (Pattern) 잘못된 회원가입 요청 테스트")
	void signUpRequestFailure_Pattern() {
		// Validation Pattern Test
		UserSignupRequest patternTestRequest = new UserSignupRequest(
				"testtestT",
				"password1234A",
				"nickname",
				"02-000-00000",
				"test@email"
		);
		
		Set<ConstraintViolation<UserSignupRequest>> patternViolations = validator.validate(patternTestRequest);
		
		assertThat(patternViolations).isNotEmpty();
		
		List<String> patternErrorFields = patternViolations.stream()
				.map(v -> v.getPropertyPath().toString())
				.toList();
		
		assertThat(patternErrorFields).containsExactlyInAnyOrder(
				"loginId", "password", "phone", "email"
		);
	}
	
	@Test
	@DisplayName("validate: (Null) 잘못된 회원가입 요청 테스트")
	void signUpRequestFailure_Null() {
		// Validation Null Test
		UserSignupRequest nullTestRequest = new UserSignupRequest(null, null, null, null, null);
		
		Set<ConstraintViolation<UserSignupRequest>> nullViolations = validator.validate(nullTestRequest);
		
		List<String> nullErrorFields = nullViolations.stream()
				.map(v -> v.getPropertyPath().toString())
				.toList();
		
		assertThat(nullErrorFields).containsExactlyInAnyOrder(
				"loginId", "password", "nickname", "phone", "email"
		);
	}
	
}
