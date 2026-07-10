package com.georgia.jeogiyo.user.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.georgia.jeogiyo.user.dto.request.UserSearchRequest;
import com.georgia.jeogiyo.user.dto.response.UserInfoResponse;
import com.georgia.jeogiyo.user.entity.QUser;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryDslRepository {

	private final JPAQueryFactory queryFactory;
	
	public List<UserInfoResponse> findByRoleAndKeyword(UserSearchRequest userSearch) {
		QUser user = QUser.user;
		
		List<UserInfoResponse> search = queryFactory
				.select(Projections.constructor(UserInfoResponse.class,
						user.userId,
						user.loginId,
						user.nickname,
						user.phone,
						user.email,
						user.role
				))
				.from(user)
				.where(
						roleEq(userSearch),
						keywordContains(userSearch),
						user.isDeleted.eq(false)
				)
				.orderBy(getOrderBy(userSearch))
				.offset((long) userSearch.getPage() * userSearch.getSize())
				.limit(userSearch.getSize())
				.fetch();
		
		return search;
	}
	
	private BooleanExpression roleEq(UserSearchRequest userSearch) {
		return userSearch.getRole() != null
				? QUser.user.role.eq(userSearch.getRole())
				: null;
	}
	
	private BooleanExpression keywordContains(UserSearchRequest userSearch) {
		if(userSearch.getKeyword() == null || userSearch.getKeyword().isBlank()) {
			return null;
		}
		
		return QUser.user.loginId.contains(userSearch.getKeyword())
				.or(QUser.user.nickname.contains(userSearch.getKeyword()));
		
	}
	
	// TODO: 기본 sort created_at 인데 시간남으면 확장해볼 예정
	private OrderSpecifier<?> getOrderBy(UserSearchRequest userSearch) {
		if ("asc".equalsIgnoreCase(userSearch.getSort())) {
			return QUser.user.createdAt.asc();
		}
		
		return QUser.user.createdAt.desc();
	}
	
}
