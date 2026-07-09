package com.georgia.jeogiyo.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.user.dto.request.UserSearchRequest;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.fixture.UserFix;
import com.georgia.jeogiyo.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
public class UserSearchTest {

	@Autowired
	private UserService userCommandService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserFinder userFinder;
	
	@Autowired
	private EntityManager em;
	
	@Autowired
	private UserFinderService userFinderService;
	
	private List<UserSignupRequest> testUserListNameTest = List.of(
			new UserSignupRequest("test01", "Password01@", "nickname01", "02-000-0000", "test01@email.com"),
			new UserSignupRequest("test02", "Password01@", "nickname02", "02-000-0000", "test02@email.com"),
			new UserSignupRequest("test03", "Password01@", "nickname03", "02-000-0000", "test03@email.com"),
			new UserSignupRequest("test04", "Password01@", "nickname04", "02-000-0000", "test04@email.com"),
			new UserSignupRequest("test05", "Password01@", "nickname05", "02-000-0000", "test05@email.com"),
			new UserSignupRequest("test06", "Password01@", "nickname06", "02-000-0000", "test06@email.com"),
			new UserSignupRequest("test07", "Password01@", "nickname07", "02-000-0000", "test07@email.com"),
			new UserSignupRequest("test08", "Password01@", "nickname08", "02-000-0000", "test08@email.com"),
			new UserSignupRequest("test09", "Password01@", "nickname09", "02-000-0000", "test09@email.com"),
			new UserSignupRequest("test10", "Password01@", "nickname10", "02-000-0000", "test10@email.com"),
			new UserSignupRequest("test11", "Password01@", "nickname11", "02-000-0000", "test11@email.com"),
			new UserSignupRequest("test12", "Password01@", "nickname12", "02-000-0000", "test12@email.com")
	);
	
	private List<UserSignupRequest> testUserListNameMath = List.of(
			new UserSignupRequest("math01", "Password01@", "math0001", "02-000-0000", "math01@email.com"),
			new UserSignupRequest("math02", "Password01@", "math0002", "02-000-0000", "math02@email.com"),
			new UserSignupRequest("math03", "Password01@", "math0003", "02-000-0000", "math03@email.com"),
			new UserSignupRequest("math04", "Password01@", "math0004", "02-000-0000", "math04@email.com"),
			new UserSignupRequest("math05", "Password01@", "math0005", "02-000-0000", "math05@email.com"),
			new UserSignupRequest("math06", "Password01@", "math0006", "02-000-0000", "math06@email.com"),
			new UserSignupRequest("math07", "Password01@", "math0007", "02-000-0000", "math07@email.com"),
			new UserSignupRequest("math08", "Password01@", "math0008", "02-000-0000", "math08@email.com"),
			new UserSignupRequest("math09", "Password01@", "math0009", "02-000-0000", "math09@email.com"),
			new UserSignupRequest("math10", "Password01@", "math0010", "02-000-0000", "math10@email.com"),
			new UserSignupRequest("math11", "Password01@", "math0011", "02-000-0000", "math11@email.com"),
			new UserSignupRequest("math12", "Password01@", "math0012", "02-000-0000", "math12@email.com"),
			new UserSignupRequest("math13", "Password01@", "math0013", "02-000-0000", "math13@email.com")
	);
	
	private String masterLoginId;
	
	@Autowired
	private UserRepository userRepository;
	
	@BeforeEach
	void setUp() {
		UserSignupRequest signupRequest = UserFix.getUserSignupRequest();
		
		User user = User.create(signupRequest, passwordEncoder);
		
		user.changeRole(Role.MASTER);
		
		User saved = userRepository.save(user);
		
		masterLoginId = saved.getLoginId();
		
		testUserListNameTest.stream()
		.forEach(u -> {
			userCommandService.signup(u);
		});
		
		testUserListNameMath.stream()
		.forEach(u -> {
			userCommandService.signup(u);
		});
		
		em.flush();
		em.clear();
	}
	
	@Test
	@DisplayName("service-search: 회원 목록 검색 테스트")
	void nameTestSearchTest() {
		UserSearchRequest searchRequest = new UserSearchRequest();
		
		int page0NameTestUserCount = 10;
		int page1NameTestUserCount = 2;
		int page0NameMathUserCount = 10;
		int page1NameMathUserCount = 3;
		int ownerUserCount = 0;
		int masterUserCount = 1;
		
		searchRequest.setRole(Role.CUSTOMER);
		searchRequest.setKeyword("test");
		searchRequest.setPage(0);
		searchRequest.setSize(10);
		searchRequest.setSort("desc");
		
		List<UserInfoResponse> userListNameTest10 = userFinderService.getUserList(masterLoginId, searchRequest);
		
		assertThat(userListNameTest10).hasSize(page0NameTestUserCount);
		
		searchRequest.setPage(1);
		
		List<UserInfoResponse> userListNameTest2 = userFinderService.getUserList(masterLoginId, searchRequest);
		
		assertThat(userListNameTest2).hasSize(page1NameTestUserCount);
		
		searchRequest.setKeyword("math");
		searchRequest.setPage(0);
		
		List<UserInfoResponse> userListNameMath10 = userFinderService.getUserList(masterLoginId, searchRequest);
		
		assertThat(userListNameMath10).hasSize(page0NameMathUserCount);
		
		searchRequest.setPage(1);
		
		List<UserInfoResponse> userListNameMath2 = userFinderService.getUserList(masterLoginId, searchRequest);
		
		assertThat(userListNameMath2).hasSize(page1NameMathUserCount);
		
		searchRequest.setRole(Role.OWNER);
		
		List<UserInfoResponse> ownerUserList0 = userFinderService.getUserList(masterLoginId, searchRequest);
		
		assertThat(ownerUserList0).hasSize(ownerUserCount);
		
		searchRequest.setRole(Role.MASTER);
		searchRequest.setKeyword(masterLoginId);
		searchRequest.setPage(0);
		
		List<UserInfoResponse> masterUserList1 = userFinderService.getUserList(masterLoginId, searchRequest);
		
		assertThat(masterUserList1).hasSize(masterUserCount);
	}
	
}
