package com.georgia.jeogiyo.category.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CategoryValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory =
                Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @Test
    @DisplayName("validate: 올바른 카테고리 생성 요청 테스트")
    void createCategoryRequestSuccess() {
        // given
        CategoryCreateRequest request =
                new CategoryCreateRequest("한식");

        // when
        Set<ConstraintViolation<CategoryCreateRequest>> violations =
                validator.validate(request);

        // 출력
        System.out.println("========== 정상 요청 검증 ==========");
        System.out.println("categoryName: "
                + request.getCategoryName());
        System.out.println("검증 오류 개수: "
                + violations.size());

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("validate: 빈 카테고리 이름 요청 실패 테스트")
    void createCategoryRequestFailure_Blank() {
        // given
        CategoryCreateRequest request =
                new CategoryCreateRequest("");

        // when
        Set<ConstraintViolation<CategoryCreateRequest>> violations =
                validator.validate(request);

        List<String> errorFields = violations.stream()
                .map(violation ->
                        violation.getPropertyPath().toString())
                .toList();

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        // 출력
        System.out.println("========== 빈 문자열 검증 ==========");
        System.out.println("categoryName: \"\"");
        System.out.println("오류 필드: " + errorFields);
        System.out.println("오류 메시지: " + errorMessages);

        // then
        assertThat(violations).isNotEmpty();

        assertThat(errorFields)
                .contains("categoryName");

        assertThat(errorMessages)
                .contains("카테고리 이름은 필수입니다.");
    }

    @Test
    @DisplayName("validate: 공백 카테고리 이름 요청 실패 테스트")
    void createCategoryRequestFailure_Whitespace() {
        // given
        CategoryCreateRequest request =
                new CategoryCreateRequest("   ");

        // when
        Set<ConstraintViolation<CategoryCreateRequest>> violations =
                validator.validate(request);

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        // 출력
        System.out.println("========== 공백 문자열 검증 ==========");
        System.out.println("categoryName: \"   \"");
        System.out.println("오류 메시지: " + errorMessages);

        // then
        assertThat(violations).isNotEmpty();

        assertThat(errorMessages)
                .contains("카테고리 이름은 필수입니다.");
    }

    @Test
    @DisplayName("validate: null 카테고리 이름 요청 실패 테스트")
    void createCategoryRequestFailure_Null() {
        // given
        CategoryCreateRequest request =
                new CategoryCreateRequest(null);

        // when
        Set<ConstraintViolation<CategoryCreateRequest>> violations =
                validator.validate(request);

        List<String> errorFields = violations.stream()
                .map(violation ->
                        violation.getPropertyPath().toString())
                .toList();

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        // 출력
        System.out.println("========== null 검증 ==========");
        System.out.println("categoryName: null");
        System.out.println("오류 필드: " + errorFields);
        System.out.println("오류 메시지: " + errorMessages);

        // then
        assertThat(violations).isNotEmpty();

        assertThat(errorFields)
                .contains("categoryName");

        assertThat(errorMessages)
                .contains("카테고리 이름은 필수입니다.");
    }

    @Test
    @DisplayName("validate: 50자를 초과한 카테고리 이름 요청 실패 테스트")
    void createCategoryRequestFailure_Size() {
        // given
        String categoryName = "가".repeat(51);

        CategoryCreateRequest request =
                new CategoryCreateRequest(categoryName);

        // when
        Set<ConstraintViolation<CategoryCreateRequest>> violations =
                validator.validate(request);

        List<String> errorFields = violations.stream()
                .map(violation ->
                        violation.getPropertyPath().toString())
                .toList();

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        // 출력
        System.out.println("========== 길이 초과 검증 ==========");
        System.out.println("categoryName 길이: "
                + categoryName.length());
        System.out.println("오류 필드: " + errorFields);
        System.out.println("오류 메시지: " + errorMessages);

        // then
        assertThat(violations).isNotEmpty();

        assertThat(errorFields)
                .contains("categoryName");

        assertThat(errorMessages)
                .contains("카테고리 이름은 최대 50자까지 입력할 수 있습니다.");
    }

    @Test
    @DisplayName("validate: 카테고리 이름 50자 경계값 성공 테스트")
    void createCategoryRequestSuccess_MaxSize() {
        // given
        String categoryName = "가".repeat(50);

        CategoryCreateRequest request =
                new CategoryCreateRequest(categoryName);

        // when
        Set<ConstraintViolation<CategoryCreateRequest>> violations =
                validator.validate(request);

        // 출력
        System.out.println("========== 50자 경계값 검증 ==========");
        System.out.println("categoryName 길이: "
                + categoryName.length());
        System.out.println("검증 오류 개수: "
                + violations.size());

        // then
        assertThat(violations).isEmpty();
    }
}