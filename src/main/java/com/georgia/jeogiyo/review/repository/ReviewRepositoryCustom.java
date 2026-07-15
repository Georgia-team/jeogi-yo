package com.georgia.jeogiyo.review.repository;

import com.georgia.jeogiyo.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/*
 * Review Querydsl Custom Repository
 *
 * Spring Data JPA의 메서드 이름만으로 처리하기 어려운
 * 동적 리뷰 검색 기능을 선언한다.
 *
 * 실제 Querydsl 쿼리는 ReviewRepositoryImpl에서 구현한다.
 */
public interface ReviewRepositoryCustom {

    /*
     * 가게별 리뷰 목록 검색
     *
     * storeId:
     * 리뷰를 조회할 가게의 고유번호
     *
     * rating:
     * 평점 검색 조건
     * null이면 평점 조건 없이 전체 리뷰를 조회한다.
     *
     * pageable:
     * 페이지 번호, 페이지 크기, 정렬 정보를 전달한다.
     */
    Page<Review> searchReviews(
            UUID storeId,
            Integer rating,
            Pageable pageable
    );
}