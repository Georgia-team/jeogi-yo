package com.georgia.jeogiyo.user.service;

import com.georgia.jeogiyo.user.entity.User;

public interface UserFinder {
	User getUserById(String userId);
	
	User getUserByLoginId(String loginId);
}
