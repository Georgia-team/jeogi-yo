package com.georgia.jeogiyo.store.entity;

import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.global.entity.BaseEntity;
import com.georgia.jeogiyo.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_store")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID storeId;

    // N(Store) : 1(User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // N(Store) : 1(Category)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName;

    @Column(name = "address", nullable = false, length = 255)
    private String address;
    /*
       TODO 주소 체계 - 현재 적용 :
        - p_store.address 유지
        - 가게 등록/수정 + 주문시 address가 광화문 근처인지 Service에서 검증예상

       주소 체계 - 추후 확장(IDEA):
        - p_region 테이블 추가, region_id FK 연결
        - road_address/detail_address/zipcode 분리
        - 위도/경도 기반 거리 검증
    */

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    // TODO status default : open or closed 재확인
    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", nullable = false)
    private StoreStatus storeStatus  = StoreStatus.CLOSED;

    public Store(User owner, Category category, String storeName, String address, String phone) {
        this.owner = owner;
        this.category = category;
        this.storeName = storeName;
        this.address = address;
        this.phone = phone;
        this.storeStatus = StoreStatus.CLOSED;
    }

    // 가게 정보 수정
    public void update(Category category, String storeName, String address, String phone) {
        if (category != null) {
            this.category = category;
        }

        if (storeName != null) {
            this.storeName = storeName;
        }

        if (address != null) {
            this.address = address;
        }

        if (phone != null) {
            this.phone = phone;
        }
    }

    // 가게 상태 변경
    public void changeStatus(StoreStatus storeStatus) {
        if (this.storeStatus == StoreStatus.OUT_OF_BUSINESS) {
            throw new IllegalArgumentException("폐업 상태의 가게는 상태를 변경할 수 없습니다.");
        }

        this.storeStatus = storeStatus;
    }
}