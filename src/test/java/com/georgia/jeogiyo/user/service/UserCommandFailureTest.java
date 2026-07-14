package com.georgia.jeogiyo.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.dto.request.UserDeleteRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.request.UserUpdateRequest;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.exception.UserDomainException;
import com.georgia.jeogiyo.user.exception.UserErrorCode;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class UserCommandFailureTest {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserService userCommandService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserFinder userFinder;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private StoreRepository storeRepository;
	
	@Autowired
	private EntityManager em;
	
	private UserSignupRequest userSignupRequest = UserFix.getUserSignupRequest();
	
	private User user;
	
	@BeforeEach
	void setUp() {
		user = userRepository.save(User.customerCreate(userSignupRequest, passwordEncoder));
	}
	
	@Test
	@DisplayName("service-fail: 회원가입 테스트 실패 케이스: 중복 이메일")
	void failSignupTest_Email() {
		UserSignupRequest failSignupRequestEmail = new UserSignupRequest(
				"failtest01",
				"Password01@",
				"failnickname",
				userSignupRequest.getPhone(),
				userSignupRequest.getEmail()
		);
		
		assertThatThrownBy(() -> userCommandService.signup(failSignupRequestEmail, Role.CUSTOMER))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_EMAIL.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원가입 테스트 실패 케이스: 중복 닉네임")
	void failSignupTest_Nickname() {
		UserSignupRequest failSignupRequestNickname = new UserSignupRequest(
				"failtest01",
				"Password01@",
				userSignupRequest.getNickname(),
				userSignupRequest.getPhone(),
				"failtest@email.com"
		);
		
		assertThatThrownBy(() -> userCommandService.signup(failSignupRequestNickname, Role.CUSTOMER))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원가입 테스트 실패 케이스: 중복 아이디")
	void failSignupTest_LoginId() {
		UserSignupRequest failSignupRequestNickname = new UserSignupRequest(
				userSignupRequest.getLoginId(),
				"Password01@",
				"failnickname",
				userSignupRequest.getPhone(),
				"failtest@email.com"
		);
		
		assertThatThrownBy(() -> userCommandService.signup(failSignupRequestNickname, Role.CUSTOMER))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATION_LOGIN_ID.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원 수정 테스트 실패 케이스: 중복 닉네임")
	void failUpdateTest_Nickname() {
		UserUpdateRequest failUpdateRequestNickname = new UserUpdateRequest(
				userSignupRequest.getNickname(),
				null, null, null
		);
		
		assertThatThrownBy(() -> userCommandService.update(user.getLoginId(), failUpdateRequestNickname))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());
	}
	
	@Test
	@DisplayName("service-fail: 회원 수정 테스트 실패 케이스: 중복 이메일")
	void failUpdateTest_Email() {
		UserSignupRequest userSignupRequestGiven = new UserSignupRequest(
				"test0000",
				"password1234A@",
				"springboot",
				"02-000-0000",
				"failtest@email.com"
		);
		
		userCommandService.signup(userSignupRequestGiven, Role.CUSTOMER);
		
		User given = userFinder.getUserByLoginId(userSignupRequestGiven.getLoginId());
		
		UserUpdateRequest failUpdateRequestEmail = new UserUpdateRequest(
				null, null, userSignupRequest.getEmail(), null
		);
		
		assertThatThrownBy(() -> userCommandService.update(given.getLoginId(), failUpdateRequestEmail))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DUPLICATE_EMAIL.getMessage());
	}
	
	

	@Test
	@DisplayName("service-fail: 활성화된 가게가 있는 OWNER 유저 탈퇴 실패 케이스")
	void failDeleteOwnerUserWithActiveStoreTest() {
		user.changeRole(Role.OWNER);

		Category category = categoryRepository.save(new Category("OWNER 탈퇴 실패 카테고리"));
		storeRepository.save(new Store(
				user,
				category,
				"OWNER 탈퇴 실패 가게",
				"서울시 테스트구 테스트로 10",
				"02-1234-5678"
		));

		String loginId = user.getLoginId();
		String email = user.getEmail();
		String password = userSignupRequest.getPassword();

		em.flush();
		em.clear();

		User owner = userFinder.getUserByLoginId(loginId);

		assertThat(owner.getRole()).isEqualTo(Role.OWNER);
		assertThat(storeRepository.existsByOwner_UserIdAndIsDeletedFalse(owner.getUserId())).isTrue();

		UserDeleteRequest userDeleteRequest = new UserDeleteRequest(email, password);

		assertThatThrownBy(() -> userCommandService.delete(loginId, userDeleteRequest))
		.isInstanceOf(UserDomainException.class)
		.hasMessage(UserErrorCode.DELETE_FAILURE_OPEN_STORES.getMessage());

		em.flush();
		em.clear();

		User updated = userFinder.getUserByLoginId(loginId);

		assertThat(updated.isDeleted()).isFalse();
		assertThat(updated.getDeletedAt()).isNull();
		assertThat(updated.getDeletedBy()).isNull();
	}

	// TODO: 회원가입 실패 테스트 케이스
	
}
