package com.georgia.jeogiyo.order.service;
import com.georgia.jeogiyo.global.response.PageResponse;
import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.address.repository.AddressRepository;
import com.georgia.jeogiyo.order.dto.request.OrderCancelRequest;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.request.OrderStatusUpdateRequest;
import com.georgia.jeogiyo.order.dto.response.*;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.order.repository.OrderRepository;
import com.georgia.jeogiyo.order.dto.response.OrderCancelResponse;
import java.time.LocalDateTime;
import com.georgia.jeogiyo.orderitem.entity.OrderItem;
import com.georgia.jeogiyo.orderitem.repository.OrderItemRepository;
import com.georgia.jeogiyo.payment.repository.PaymentRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;


import java.util.*;
import jakarta.persistence.EntityManager;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final PaymentRepository paymentRepository;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.ORDER_REQUESTED, Set.of(OrderStatus.ORDER_ACCEPTED, OrderStatus.ORDER_REJECTED),
            OrderStatus.ORDER_ACCEPTED, Set.of(OrderStatus.COOKING_COMPLETED),
            OrderStatus.COOKING_COMPLETED, Set.of(OrderStatus.DELIVERY_PICKED_UP),
            OrderStatus.DELIVERY_PICKED_UP, Set.of(OrderStatus.DELIVERED)
    );

    public OrderService(OrderRepository orderRepository, AddressRepository addressRepository,ProductRepository productRepository,OrderItemRepository orderItemRepository, StoreRepository storeRepository,UserRepository userRepository,JPAQueryFactory queryFactory,EntityManager entityManager,PaymentRepository paymentRepository) {

        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.queryFactory = queryFactory;
        this.entityManager = entityManager;
        this.paymentRepository = paymentRepository;
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

        Address address = addressRepository.findByUserAndAddressIdAndIsDeletedFalse(user, orderCreateRequest.getAddressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "배송지를 찾을 수 없습니다."));


        if (!isServiceableArea(address.getRoadAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "서비스 가능 지역이 아닙니다.");
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

    public PageResponse<OrderSearchResponse> searchOrders(String loginId, OrderStatus orderStatus, Pageable pageable) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

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

        return PageResponse.from(orderPage, order -> {
            Store store = storeRepository.findById(order.getStoreId()).orElse(null);

            OrderSearchResponse item = new OrderSearchResponse();
            item.setOrderId(order.getOrderId());
            item.setStoreId(order.getStoreId());
            item.setStoreName(store != null ? store.getStoreName() : null);
            item.setOrderStatus(order.getOrderStatus().name());
            item.setTotalPrice(order.getTotalPrice());
            item.setCreatedAt(order.getCreatedAt());
            return item;
        });
    }

    public PageResponse<OrderStoreSearchResponse> searchOrdersByStore(String loginId, UUID storeId, OrderStatus orderStatus, Pageable pageable) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));

        if (user.getRole() == Role.OWNER) {
            if (!store.getOwner().getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 가게의 주문만 조회할 수 있습니다.");
            }
        } else if (user.getRole() != Role.MASTER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다.");
        }

        Page<Order> orderPage = orderRepository.searchOrdersByStore(storeId, orderStatus, pageable);

        return PageResponse.from(orderPage, order -> {
            User customer = userRepository.findById(order.getUserId()).orElse(null);

            OrderStoreSearchResponse item = new OrderStoreSearchResponse();
            item.setOrderId(order.getOrderId());
            item.setCustomerName(customer != null ? customer.getNickname() : null);
            item.setOrderStatus(order.getOrderStatus().name());
            item.setTotalPrice(order.getTotalPrice());
            item.setCreatedAt(order.getCreatedAt());
            return item;
        });

    }
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(String loginId, UUID orderId, OrderStatusUpdateRequest request) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (user.getRole() != Role.OWNER && user.getRole() != Role.MASTER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "변경 권한이 없습니다.");
        }

        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (user.getRole() == Role.OWNER) {
            Store store = storeRepository.findByStoreIdAndIsDeletedFalse(order.getStoreId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));
            if (!store.getOwner().getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 가게의 주문만 변경할 수 있습니다.");
            }
        }

        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus nextStatus = request.getOrderStatus();

        Set<OrderStatus> allowedNext = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(nextStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "허용되지 않은 상태 변경입니다.");
        }

        order.changeStatus(nextStatus);
        entityManager.flush();

        OrderStatusUpdateResponse response = new OrderStatusUpdateResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setUpdatedAt(order.getUpdatedAt());

        return response;
    }

    @Transactional
    public OrderCancelResponse cancelOrder(String loginId, UUID orderId, OrderCancelRequest request) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (user.getRole() != Role.CUSTOMER && user.getRole() != Role.MASTER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "취소 권한이 없습니다.");
        }

        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (user.getRole() == Role.CUSTOMER) {
            if (!order.getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 주문만 취소할 수 있습니다.");
            }
            if (order.getOrderStatus() != OrderStatus.ORDER_REQUESTED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "취소할 수 없는 상태의 주문입니다.");
            }
            if (order.getCreatedAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "주문 후 5분이 지나 취소할 수 없습니다.");
            }
        } else {
            if (order.getOrderStatus() == OrderStatus.DELIVERED
                    || order.getOrderStatus() == OrderStatus.CANCELLED
                    || order.getOrderStatus() == OrderStatus.ORDER_REJECTED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "취소할 수 없는 상태의 주문입니다.");
            }
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Product product = productRepository.findByProductIdAndIsDeletedFalse(item.getProductId()).orElse(null);
            if (product != null) {
                product.restoreStock(item.getQuantity());
            }
        }

        paymentRepository.findByOrder_OrderIdAndIsDeletedFalse(orderId).ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                payment.cancel(request.getCancelReason());
            }
        });

        order.changeStatus(OrderStatus.CANCELLED);
        entityManager.flush();

        OrderCancelResponse response = new OrderCancelResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderStatus(order.getOrderStatus().name());
        response.setCanceledAt(order.getUpdatedAt());
        response.setCancelReason(request.getCancelReason());

        return response;
    }

    @Transactional
    public void cancelByPayment(UUID orderId, String loginId) {

        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (order.getOrderStatus() != OrderStatus.ORDER_REQUESTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "취소할 수 없는 상태의 주문입니다.");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Product product = productRepository.findByProductIdAndIsDeletedFalse(item.getProductId()).orElse(null);
            if (product != null) {
                product.restoreStock(item.getQuantity());
            }
        }

        order.changeStatus(OrderStatus.CANCELLED);
    }

}