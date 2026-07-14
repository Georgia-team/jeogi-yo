package com.georgia.jeogiyo.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

import com.georgia.jeogiyo.category.dto.request.CategoryCreateRequest;
import com.georgia.jeogiyo.category.dto.request.CategoryUpdateRequest;
import com.georgia.jeogiyo.category.dto.response.CategoryCreateResponse;
import com.georgia.jeogiyo.category.dto.response.CategoryReadResponse;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.category.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager em;

    private final String loginId = "master_jun";

    @Test
    @DisplayName("service: 카테고리 생성(Create) 테스트")
    void createCategoryTest() {
        CategoryCreateRequest request = new CategoryCreateRequest("감자튀김1");

        CategoryCreateResponse response = categoryService.createCategory(request);

        em.flush();
        em.clear();

        Category savedCategory = categoryRepository
                .findById(response.getCategoryId())
                .orElseThrow();

        System.out.println("=== 카테고리 생성 결과 ===");
        System.out.println("categoryId = " + response.getCategoryId());
        System.out.println("categoryName = " + response.getCategoryName());
        System.out.println("createdAt = " + savedCategory.getCreatedAt());

        assertThat(response.getCategoryId()).isNotNull();
        assertThat(response.getCategoryName()).isEqualTo("감자튀김1");

        assertThat(savedCategory.getCategoryName()).isEqualTo("감자튀김1");
        assertThat(savedCategory.isDeleted()).isFalse();
        assertThat(savedCategory.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("service: 카테고리 이름 앞뒤 공백 제거 테스트")
    void createCategoryTrimTest() {
        CategoryCreateRequest request = new CategoryCreateRequest("  파스타1  ");

        CategoryCreateResponse response =
                categoryService.createCategory(request);

        System.out.println("trim() 동작 후의 categoryName = \"" + response.getCategoryName() + "\"");

        assertThat(response.getCategoryName()).isEqualTo("파스타1");
    }

    @Test
    @DisplayName("service: 중복 카테고리 생성 실패 테스트")
    void createDuplicateCategoryTest() {
        // given: 같은 이름의 카테고리를 먼저 생성한다.
        categoryService.createCategory(new CategoryCreateRequest("파스타1"));

        // when: 동일한 이름으로 다시 생성하면 예외가 발생한다.
        Throwable throwable = catchThrowable(
                () -> categoryService.createCategory(
                        new CategoryCreateRequest("파스타1")
                )
        );

        // 출력
        System.out.println("발생한 예외 타입 = "
                + throwable.getClass().getSimpleName());
        System.out.println("발생한 예외 메시지 = "
                + throwable.getMessage());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는");
    }

    @Test
    @DisplayName("service: 카테고리 상세 조회(Read) 테스트")
    void readCategoryTest() {
        CategoryCreateResponse created =
                categoryService.createCategory(
                        new CategoryCreateRequest("닭강정1")
                );

        CategoryReadResponse response = categoryService.readResponse(created.getCategoryId());

        assertThat(response.getCategoryId())
                .isEqualTo(created.getCategoryId());

        // 근데 사실상 카테고리 read가 의미가 있나 싶다.
        System.out.println("Read 결과: " + response.getCategoryId());
        assertThat(response.getCategoryName())
                .isEqualTo("닭강정1");
    }

    @Test
    @DisplayName("service: 존재하지 않는 카테고리 조회 실패 테스트")
    void readCategoryNotFoundTest() {
        UUID categoryId = UUID.randomUUID();

        assertThatThrownBy(
                () -> categoryService.readResponse(categoryId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는");
    }

    @Test
    @DisplayName("service: 카테고리 수정(Update) 테스트")
    void updateCategoryTest() {
        CategoryCreateResponse created =
                categoryService.createCategory(
                        new CategoryCreateRequest("닭강정1")
                );

        UUID categoryId = created.getCategoryId();

        categoryService.updateCategory(
                categoryId,
                new CategoryUpdateRequest("파스타1")
        );

        String categoryName_temp = created.getCategoryName();

        em.flush();
        em.clear();

        Category updatedCategory = categoryRepository
                .findById(categoryId)
                .orElseThrow();

        System.out.println();
        System.out.println("=== 수정 전 ===");
        System.out.println("categoryId = " + categoryId);
        System.out.println("categoryName = " + categoryName_temp);
        System.out.println();
        System.out.println("=== 수정 후 ===");
        System.out.println("categoryId = " + updatedCategory.getCategoryId());
        System.out.println("categoryName = " + updatedCategory.getCategoryName());
        System.out.println("updatedAt = " + updatedCategory.getUpdatedAt());

        assertThat(updatedCategory.getCategoryName())
                .isEqualTo("파스타1");

        assertThat(updatedCategory.getUpdatedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("service: 중복 이름으로 카테고리 수정 실패 테스트")
    void updateDuplicateCategoryTest() {
        CategoryCreateResponse first =
                categoryService.createCategory(
                        new CategoryCreateRequest("파스타1")
                );

        categoryService.createCategory(
                new CategoryCreateRequest("회2")
        );

        assertThatThrownBy(
                () -> categoryService.updateCategory(
                        first.getCategoryId(),
                        new CategoryUpdateRequest("회2")
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는");
    }

    @Test
    @DisplayName("service: 카테고리 삭제 테스트")
    void deleteCategoryTest() {
        CategoryCreateResponse created =
                categoryService.createCategory(
                        new CategoryCreateRequest("파스타1파스타1")
                );

        UUID categoryId = created.getCategoryId();

        categoryService.deleteCategory(categoryId, loginId);

        em.flush();
        em.clear();

        Category deletedCategory = categoryRepository
                .findById(categoryId)
                .orElseThrow();

        System.out.println("=== 삭제 후 ===");
        System.out.println("categoryId = " + deletedCategory.getCategoryId());
        System.out.println("categoryName = " + deletedCategory.getCategoryName());
        System.out.println("isDeleted = " + deletedCategory.isDeleted());
        System.out.println("deletedAt = " + deletedCategory.getDeletedAt());
        System.out.println("deletedBy = " + deletedCategory.getDeletedBy());

        assertThat(deletedCategory.isDeleted()).isTrue();
        assertThat(deletedCategory.getDeletedAt()).isNotNull();
        assertThat(deletedCategory.getDeletedBy()).isEqualTo(loginId);
    }

    @Test
    @DisplayName("service: 이미 삭제된 카테고리 재삭제 실패 테스트")
    void deleteCategoryAgainTest() {
        CategoryCreateResponse created =
                categoryService.createCategory(
                        new CategoryCreateRequest("파스타1")
                );

        UUID categoryId = created.getCategoryId();

        categoryService.deleteCategory(categoryId, loginId);

        assertThatThrownBy(
                () -> categoryService.deleteCategory(categoryId, loginId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 삭제된");
    }

    @Test
    @DisplayName("service: 삭제된 카테고리는 상세 조회되지 않는지 테스트")
    void deletedCategoryReadFailTest() {
        CategoryCreateResponse created =
                categoryService.createCategory(
                        new CategoryCreateRequest("삭제 후 조회 테스트")
                );

        UUID categoryId = created.getCategoryId();

        categoryService.deleteCategory(categoryId, loginId);

        assertThatThrownBy(
                () -> categoryService.readResponse(categoryId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는");
    }

    @Test
    @DisplayName("service: 여러 카테고리가 DB에 저장되는지 테스트")
    void saveMultipleCategoriesTest() throws InterruptedException {
        categoryService.createCategory(
                new CategoryCreateRequest("파스타1")
        );
        Thread.sleep(1000);
        categoryService.createCategory(
                new CategoryCreateRequest("닭강정1")
        );
        Thread.sleep(2000);
        categoryService.createCategory(
                new CategoryCreateRequest("햄버거2")
        );
        Thread.sleep(3000);
        CategoryCreateResponse created =
                categoryService.createCategory(
                        new CategoryCreateRequest("회2")
                );
        categoryService.deleteCategory(created.getCategoryId(), loginId);

        em.flush();
        em.clear();

        List<Category> categories = categoryRepository.findAll();

        System.out.println("=== DB에 저장된 카테고리 목록 ===");

        for (Category category : categories) {
            System.out.println(
                    "categoryId = " + category.getCategoryId()
                            + ", categoryName = " + category.getCategoryName()
                            + ", isDeleted = " + category.isDeleted()
                            + ", createdAt = " + category.getCreatedAt()
            );
        }

        assertThat(categories)
                .extracting(Category::getCategoryName)
                .contains("파스타1", "닭강정1", "햄버거2", "회2");
    }
}