package com.georgia.jeogiyo.user.entity;

public enum Role {
	CUSTOMER(Authority.CUSTOMER),  // 일반 고객
	OWNER(Authority.OWNER),        // 사장님(가게 주인)
	MANAGER(Authority.MANAGER),    // 매니저
	MASTER(Authority.MASTER);      // 최고 관리자

	private final String authority;

	Role(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return this.authority;
	}

	public static class Authority {
		public static final String CUSTOMER = "ROLE_CUSTOMER";
		public static final String OWNER = "ROLE_OWNER";
		public static final String MANAGER = "ROLE_MANAGER";
		public static final String MASTER = "ROLE_MASTER";
	}
}
