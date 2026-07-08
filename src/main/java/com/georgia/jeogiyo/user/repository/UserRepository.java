package com.georgia.jeogiyo.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.georgia.jeogiyo.user.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findByLoginId(String loginId);

	boolean existsByEmail(String email);
	
	boolean existsByNickname(String nickname);
	
	boolean existsByLoginId(String loginId);
	
}
