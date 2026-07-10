package com.georgia.jeogiyo.category.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("service: 카테고리 생성 테스트")
    void createCategoryTest() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("한식");
        String loginId = "GUEST";

        // when
        CategoryCreateResponse response =
                categoryService.createCategory(request, loginId);

        /*
         * INSERT 쿼리를 DB에 즉시 반영하고
         * 영속성 컨텍스트를 비운다.
         *
         * 이후 조회하면 메모리에 있던 Entity가 아니라
         * 실제 DB에 저장된 데이터를 다시 가져온다.
         */
        em.flush();
        em.clear();

        Category savedCategory = categoryRepository
                .findById(response.getCategoryId())
                .orElseThrow(() ->
                        new AssertionError("저장된 카테고리를 찾을 수 없습니다."));

        // 출력
        System.out.println("========== REQUEST ==========");
        System.out.println("요청 카테고리 이름: " + request.getCategoryName());
        System.out.println("요청 사용자 loginId: " + loginId);

        System.out.println();
        System.out.println("========== RESPONSE ==========");
        System.out.println("응답 카테고리 ID: " + response.getCategoryId());
        System.out.println("응답 카테고리 이름: " + response.getCategoryName());
        System.out.println("응답 생성 시간: " + response.getCreatedAt());

        System.out.println();
        System.out.println("========== SAVED CATEGORY ==========");
        System.out.println("저장된 카테고리 ID: "
                + savedCategory.getCategoryId());
        System.out.println("저장된 카테고리 이름: "
                + savedCategory.getCategoryName());
        System.out.println("생성한 사용자: "
                + savedCategory.getCreatedBy());
        System.out.println("생성 시간: "
                + savedCategory.getCreatedAt());
        System.out.println("수정 시간: "
                + savedCategory.getUpdatedAt());
        System.out.println("수정한 사용자: "
                + savedCategory.getUpdatedBy());
        System.out.println("삭제 여부: "
                + savedCategory.isDeleted());
        System.out.println("삭제 시간: "
                + savedCategory.getDeletedAt());
        System.out.println("삭제한 사용자: "
                + savedCategory.getDeletedBy());

        // then: 응답값 검증
        assertThat(response.getCategoryId()).isNotNull();
        assertThat(response.getCategoryName())
                .isEqualTo(request.getCategoryName());
        assertThat(response.getCreatedAt()).isNotNull();

        // then: 실제 DB 저장값 검증
        assertThat(savedCategory).isNotNull();

        assertThat(savedCategory.getCategoryId())
                .isEqualTo(response.getCategoryId());

        assertThat(savedCategory.getCategoryName())
                .isEqualTo(request.getCategoryName());

        assertThat(savedCategory.getCreatedAt())
                .isNotNull();

        // TODO: 인증 정보 연동 완료 시 수정
        assertThat(savedCategory.getCreatedBy())
                .isEqualTo(loginId);

        assertThat(savedCategory.getUpdatedAt())
                .isNotNull();

        // TODO: 인증 정보 연동 완료 시 수정
        assertThat(savedCategory.getUpdatedBy())
                .isEqualTo(loginId);

        assertThat(savedCategory.isDeleted())
                .isFalse();

        assertThat(savedCategory.getDeletedAt())
                .isNull();

        assertThat(savedCategory.getDeletedBy())
                .isNull();
    }
}