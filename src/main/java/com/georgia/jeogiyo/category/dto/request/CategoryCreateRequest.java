package com.georgia.jeogiyo.category.dto.request;

/*
 * 카테고리 이름은 최대 50자까지 입력 -> { CategoryCreateRequest에서 @Size 에서 검사 }
 * 카테고리 이름은 중복될 수 없다 -> { CategoryService에서 검사 }
 *     - 카테고리 이름이 중복될 경우 409 Conflict를 반환합니다.
 *     - Soft Delete된 카테고리와 동일한 이름도 재사용할 수 없습니다.
 * MASTER만 카테고리를 생성할 수 있습니다.
 * 생성 시 created_at -> { BaseEntity에서 JPA Auditing이 자동으로 값을 넣어주는 @CreatedDate 있음 }
 *        created_by(login_id)를 저장합니다. -> { CategoryService 에서 저장 }
 */


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 50, message = "카테고리 이름은 최대 50자까지 입력할 수 있습니다.")
    private String categoryName;
}
