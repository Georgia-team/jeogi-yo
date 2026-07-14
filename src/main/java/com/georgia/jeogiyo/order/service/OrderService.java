package com.georgia.jeogiyo.order.service;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.address.repository.AddressRepository;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.response.OrderCreateResponse;
import com.georgia.jeogiyo.order.dto.response.OrderDetailResponse;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.repository.OrderRepository;
import com.georgia.jeogiyo.orderitem.entity.OrderItem;
import com.georgia.jeogiyo.orderitem.repository.OrderItemRepository;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.product.repository.ProductRepository;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.entity.StoreStatus;
import com.georgia.jeogiyo.store.repository.StoreRepository;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.georgia.jeogiyo.store.entity.QStore;
import com.georgia.jeogiyo.order.dto.response.OrderSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;

    public OrderService(OrderRepository orderRepository, AddressRepository addressRepository,ProductRepository productRepository,OrderItemRepository orderItemRepository, StoreRepository storeRepository,UserRepository userRepository,JPAQueryFactory queryFactory) {

        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.queryFactory = queryFactory;
    }
    public Order getOrder(UUID orderId){
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));
        return order;

    }
    @Transactional
    public OrderCreateResponse createOrder(String loginId, OrderCreateRequest orderCreateRequest) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        UUID userId = user.getUserId();

        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 주문을 생성할 수 있습니다.");
        }

        Store store = storeRepository.findById(orderCreateRequest.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));

        if (store.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");
        }
        if (store.getStoreStatus() != StoreStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "영업 중이 아닌 가게입니다.");
        }

        Address address = addressRepository.findById(orderCreateRequest.getAddressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "배송지를 찾을 수 없습니다."));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 배송지가 아닙니다.");
        }
        if (!isServiceableArea(address.getRoadAddress())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "서비스 가능 지역이 아닙니다.");
        }

        Integer totalPrice = 0;
        for (OrderCreateRequest.OrderItemRequest item : orderCreateRequest.getItems()) {
            Product product = productRepository.findByProductIdAndIsDeletedFalse(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

            if (!product.getStore().getStoreId().equals(store.getStoreId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "요청한 가게에 속하지 않은 상품입니다.");
            }
            if (!product.isOrderable()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "주문할 수 없는 상품입니다.");
            }
            product.decreaseStock(item.getQuantity());

            Integer itemTotalPrice = product.getPrice() * item.getQuantity();
            totalPrice += itemTotalPrice;
        }

        Order order = new Order(userId, orderCreateRequest.getStoreId(), orderCreateRequest.getAddressId(),
                address.getRoadAddress(), address.getDetailAddress(), address.getZipcode(),
                totalPrice, OrderStatus.ORDER_REQUESTED);
        Order savedOrder = orderRepository.save(order);

        for (OrderCreateRequest.OrderItemRequest item : orderCreateRequest.getItems()) {
            Product product = productRepository.findByProductIdAndIsDeletedFalse(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));
            Integer itemTotalPrice = product.getPrice() * item.getQuantity();

            OrderItem orderItem = new OrderItem(savedOrder.getOrderId(), item.getProductId(), item.getQuantity(),
                    product.getPrice(), itemTotalPrice);
            orderItemRepository.save(orderItem);
        }

        OrderCreateResponse response = new OrderCreateResponse();
        response.setOrderId(savedOrder.getOrderId());
        response.setStoreId(savedOrder.getStoreId());
        response.setAddress(savedOrder.getRoadAddress() + " " + savedOrder.getDetailAddress());
        response.setOrderStatus(savedOrder.getOrderStatus().name());
        response.setTotalPrice(savedOrder.getTotalPrice());
        response.setCreatedAt(savedOrder.getCreatedAt());

        return response;
    }

    private boolean isServiceableArea(String roadAddress) {

        return roadAddress != null && roadAddress.contains("광화문");
    }

    public OrderDetailResponse getOrderDetail(String loginId, UUID orderId) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        validateOrderAccess(user, order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        List<OrderDetailResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."));

            OrderDetailResponse.OrderItemResponse itemResponse = new OrderDetailResponse.OrderItemResponse();
            itemResponse.setProductId(item.getProductId());
            itemResponse.setProductName(product.getProductName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            itemResponse.setItemTotalPrice(item.getItemTotalPrice());
            itemResponses.add(itemResponse);
        }

        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(order.getOrderId());
        response.setStoreId(order.getStoreId());
        response.setAddressId(order.getAddressId());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setTotalPrice(order.getTotalPrice());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(itemResponses);

        return response;
    }

    private void validateOrderAccess(User user, Order order) {
        if (user.isMaster()) {
            return;
        }
        if (user.getRole() == Role.CUSTOMER) {
            if (!order.getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 주문만 조회할 수 있습니다.");
            }
            return;
        }
        if (user.isOwner()) {
            Store store = storeRepository.findById(order.getStoreId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));
            if (!store.getOwner().getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 가게의 주문만 조회할 수 있습니다.");
            }
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
    }

    public OrderSearchResponse searchOrders(String loginId, OrderStatus orderStatus, int page, int size, String sort) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        int normalizedSize = (size == 10 || size == 30 || size == 50) ? size : 10;
        int normalizedPage = Math.max(page, 0);
        Sort sortOption = "asc".equalsIgnoreCase(sort)
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize, sortOption);

        List<UUID> storeIds = null;
        if (user.getRole() == Role.OWNER) {
            storeIds = queryFactory
                    .select(QStore.store.storeId)
                    .from(QStore.store)
                    .where(QStore.store.owner.userId.eq(user.getUserId())
                            .and(QStore.store.isDeleted.isFalse()))
                    .fetch();
        }

        Page<Order> orderPage = orderRepository.searchOrders(orderStatus, user.getRole(), user.getUserId(), storeIds, pageable);

        List<OrderSearchResponse.OrderListItem> items = new ArrayList<>();
        for (Order order : orderPage.getContent()) {
            Store store = storeRepository.findById(order.getStoreId()).orElse(null);

            OrderSearchResponse.OrderListItem item = new OrderSearchResponse.OrderListItem();
            item.setOrderId(order.getOrderId());
            item.setStoreId(order.getStoreId());
            item.setStoreName(store != null ? store.getStoreName() : null);
            item.setOrderStatus(order.getOrderStatus().name());
            item.setTotalPrice(order.getTotalPrice());
            item.setCreatedAt(order.getCreatedAt());
            items.add(item);
        }

        OrderSearchResponse response = new OrderSearchResponse();
        response.setContent(items);
        response.setPage(orderPage.getNumber());
        response.setSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());

        return response;
    }

}