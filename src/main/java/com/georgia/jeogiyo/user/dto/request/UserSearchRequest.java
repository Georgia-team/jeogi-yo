package com.georgia.jeogiyo.user.dto.request;

import java.util.List;

import com.georgia.jeogiyo.user.entity.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequest {

	private Role role;
	
	private String keyword;
	
	private Integer page = 0;
	
	private Integer size = 10;
	
	private String sort = "desc";
	
	public void setSize(Integer size) {
		List<Integer> sizes = List.of(10, 30, 50);
		
		if(size == null || sizes.contains(size)) {
			this.size = 10;
		} else {
			this.size = size;
		}
	}
	
}
