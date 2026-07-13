package com.georgia.jeogiyo.user.dto.request;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.georgia.jeogiyo.user.entity.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequest {

	private Role role;
	
	@NotBlank(message = "유저 목록 검색 키워드는 필수입니다.")
	private String keyword;
	
	private Integer page = 0;
	
	private Integer size = 10;
	
	private String sort = "desc";
	
	public void setSize(Integer size) {
		List<Integer> sizes = List.of(10, 30, 50);
		
		if(size != null && sizes.contains(size)) {
			this.size = size;
		} else {
			this.size = 10;
		}
	}
	
	public Pageable toPageable(String sortProperty) {
		Sort.Direction direction = "desc".equalsIgnoreCase(this.sort)
				? Sort.Direction.DESC
				: Sort.Direction.ASC;
		
		return PageRequest.of(this.page, this.size, Sort.by(direction, sortProperty));
	}
	
}
