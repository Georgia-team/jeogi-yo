package com.georgia.jeogiyo.order.service;

import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.address.repository.AddressRepository;
import com.georgia.jeogiyo.order.dto.request.OrderCreateRequest;
import com.georgia.jeogiyo.order.dto.response.OrderCreateResponse;
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
import com.georgia.jeogiyo.user.entity.User;
import com.georgia.jeogiyo.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, AddressRepository addressRepository,ProductRepository productRepository,OrderItemRepository orderItemRepository, StoreRepository storeRepository,UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }
    public Order getOrder(UUID orderId){
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId).orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));
        return order;

    }
    @Transactional
    public OrderCreateResponse createOrder(String loginId, OrderCreateRequest orderCreateRequest) {

        // 로그인 사용자 조회
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        UUID userId = user.getUserId();

        // 가게 검증
        Store store = storeRepository.findById(orderCreateRequest.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));

        if (store.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");
        }
        if (store.getStoreStatus() != StoreStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "영업 중이 아닌 가게입니다.");
        }

        // 배송지 검증
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
            Product product = productRepository.findById(item.getProductId())
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
            Product product = productRepository.findById(item.getProductId())
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
}