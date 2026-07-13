package com.georgia.jeogiyo.order.service;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.address.repository.AddressRepository;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.response.OrderCreateResponse;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.repository.OrderRepository;
import com.georgia.jeogiyo.orderitem.repository.OrderItemRepository;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.entity.StoreStatus;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;
import com.georgia.jeogiyo.support.DomainTestFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.georgia.jeogiyo.support.DomainTestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * OrderService 단위 테스트입니다.
 * 주문 생성(createOrder)의 검증 흐름과 정상 흐름을 DB 없이 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final UUID ADDRESS_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");
    private static final UUID OTHER_ADDRESS_ID = UUID.fromString("66666666-6666-6666-6666-666666666662");
    private static final UUID ORDER_ID = UUID.fromString("77777777-7777-7777-7777-777777777771");

    @Mock private OrderRepository orderRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, addressRepository, productRepository,
                orderItemRepository, storeRepository, userRepository
        );
    }

    // ---------- 테스트 전용 fixture 헬퍼 (공용 DomainTestFixture엔 없는 것들) ----------

    private Address address(User owner, UUID addressId, String roadAddress) {
        AddressCreateRequest request = new AddressCreateRequest();
        ReflectionTestUtils.setField(request, "roadAddress", roadAddress);
        ReflectionTestUtils.setField(request, "detailAddress", "상세주소");
        ReflectionTestUtils.setField(request, "zipcode", "03150");
        ReflectionTestUtils.setField(request, "isDefault", true);

        Address address = Address.create(owner, request);
        ReflectionTestUtils.setField(address, "addressId", addressId);
        return address;
    }

    private Product product(Store store, Category category, int price, int stock, boolean hidden) {
        Product product = new Product(store, category, "테스트 상품", "설명", price, stock, hidden);
        ReflectionTestUtils.setField(product, "productId", PRODUCT_ID);
        return product;
    }

    private OrderCreateRequest orderRequest(UUID storeId, UUID addressId, UUID productId, int quantity) {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setStoreId(storeId);
        request.setAddressId(addressId);

        OrderCreateRequest.OrderItemRequest item = new OrderCreateRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        request.setItems(List.of(item));

        return request;
    }

    // ---------- 테스트 ----------

    @Test
    @DisplayName("CUSTOMER는 정상적으로 주문을 생성할 수 있다")
    void createOrder_success() {
        // given
        User customer = customer();
        Category category = category();
        Store store = store(owner(), category);
        store.changeStatus(StoreStatus.OPEN);
        Address address = address(customer, ADDRESS_ID, "서울시 종로구 광화문로 1");
        Product product = product(store, category, 12000, 30, false);
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));
        given(addressRepository.findById(ADDRESS_ID)).willReturn(Optional.of(address));
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(product));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "orderId", ORDER_ID);
            return order;
        });

        // when
        OrderCreateResponse response = orderService.createOrder(CUSTOMER_LOGIN_ID, request);

        // then
        assertThat(response.getOrderId()).isEqualTo(ORDER_ID);
        assertThat(response.getTotalPrice()).isEqualTo(24000); // 12000 * 2
        assertThat(response.getOrderStatus()).isEqualTo("ORDER_REQUESTED");
        assertThat(product.getStock()).isEqualTo(28); // 30 - 2, 재고 차감 확인
        then(orderItemRepository).should().save(any());
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 예외가 발생한다")
    void createOrder_userNotFound_fail() {
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);
        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 가게로는 주문할 수 없다")
    void createOrder_storeNotFound_fail() {
        User customer = customer();
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("가게를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("영업 중(OPEN)이 아닌 가게에는 주문할 수 없다")
    void createOrder_storeNotOpen_fail() {
        User customer = customer();
        Category category = category();
        Store store = store(owner(), category); // 기본값 CLOSED
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("영업 중이 아닌");
    }

    @Test
    @DisplayName("본인 소유가 아닌 배송지로는 주문할 수 없다")
    void createOrder_addressNotOwned_fail() {
        User customer = customer();
        User otherOwner = otherOwner();
        Category category = category();
        Store store = store(owner(), category);
        store.changeStatus(StoreStatus.OPEN);
        Address othersAddress = address(otherOwner, ADDRESS_ID, "서울시 종로구 광화문로 1");
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));
        given(addressRepository.findById(ADDRESS_ID)).willReturn(Optional.of(othersAddress));

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("본인의 배송지가 아닙니다");
    }

    @Test
    @DisplayName("서비스 가능 지역이 아닌 배송지로는 주문할 수 없다")
    void createOrder_addressNotServiceable_fail() {
        User customer = customer();
        Category category = category();
        Store store = store(owner(), category);
        store.changeStatus(StoreStatus.OPEN);
        Address farAddress = address(customer, ADDRESS_ID, "부산시 해운대구 어딘가로 1");
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));
        given(addressRepository.findById(ADDRESS_ID)).willReturn(Optional.of(farAddress));

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("서비스 가능 지역이 아닙니다");
    }

    @Test
    @DisplayName("요청한 가게에 속하지 않은 상품은 주문할 수 없다")
    void createOrder_productNotInStore_fail() {
        User customer = customer();
        Category category = category();
        Store store = store(owner(), category);
        store.changeStatus(StoreStatus.OPEN);
        Store otherStore = otherOwnerStore(otherOwner(), category);
        Address address = address(customer, ADDRESS_ID, "서울시 종로구 광화문로 1");
        Product productFromOtherStore = product(otherStore, category, 12000, 30, false);
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));
        given(addressRepository.findById(ADDRESS_ID)).willReturn(Optional.of(address));
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(productFromOtherStore));

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("속하지 않은 상품");
    }

    @Test
    @DisplayName("숨김 상품은 주문할 수 없다")
    void createOrder_hiddenProduct_fail() {
        User customer = customer();
        Category category = category();
        Store store = store(owner(), category);
        store.changeStatus(StoreStatus.OPEN);
        Address address = address(customer, ADDRESS_ID, "서울시 종로구 광화문로 1");
        Product hiddenProduct = product(store, category, 12000, 30, true);
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 2);

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));
        given(addressRepository.findById(ADDRESS_ID)).willReturn(Optional.of(address));
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(hiddenProduct));

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("주문할 수 없는 상품");
    }

    @Test
    @DisplayName("재고보다 많은 수량을 주문하면 예외가 발생한다")
    void createOrder_insufficientStock_fail() {
        User customer = customer();
        Category category = category();
        Store store = store(owner(), category);
        store.changeStatus(StoreStatus.OPEN);
        Address address = address(customer, ADDRESS_ID, "서울시 종로구 광화문로 1");
        Product lowStockProduct = product(store, category, 12000, 3, false); // 재고 3개
        OrderCreateRequest request = orderRequest(STORE_ID, ADDRESS_ID, PRODUCT_ID, 5); // 5개 주문

        given(userRepository.findByLoginId(CUSTOMER_LOGIN_ID)).willReturn(Optional.of(customer));
        given(storeRepository.findById(STORE_ID)).willReturn(Optional.of(store));
        given(addressRepository.findById(ADDRESS_ID)).willReturn(Optional.of(address));
        given(productRepository.findByProductIdAndIsDeletedFalse(PRODUCT_ID)).willReturn(Optional.of(lowStockProduct));

        assertThatThrownBy(() -> orderService.createOrder(CUSTOMER_LOGIN_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("재고가 부족합니다.");
    }
}