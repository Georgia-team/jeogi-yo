package com.georgia.jeogiyo.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByLoginId(String loginId);

	boolean existsByEmail(String email);
	
	boolean existsByNickname(String nickname);
	
	boolean existsByLoginId(String loginId);
	
	long countByRole(Role role);

	Optional<User> findByLoginIdAndIsDeleted(String loginId, boolean isDeleted);
	
	Optional<User> findByLoginIdAndIsDeletedFalse(String loginId);
}
