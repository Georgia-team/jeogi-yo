package com.georgia.jeogiyo.address.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressSearchRequest {

	private Integer page;
	
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
