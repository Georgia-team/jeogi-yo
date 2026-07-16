package com.georgia.jeogiyo.support;

import com.georgia.jeogiyo.address.dto.request.AddressCreateRequest;
import com.georgia.jeogiyo.address.entity.Address;
import com.georgia.jeogiyo.ai.dto.request.AiDescriptionRequest;
import com.georgia.jeogiyo.ai.entity.AiHistory;
import com.georgia.jeogiyo.category.entity.Category;
import com.georgia.jeogiyo.order.entity.Order;
import com.georgia.jeogiyo.order.entity.OrderStatus;
import com.georgia.jeogiyo.payment.dto.request.PaymentCancelRequest;
import com.georgia.jeogiyo.payment.dto.request.PaymentCreateRequest;
import com.georgia.jeogiyo.payment.entity.Payment;
import com.georgia.jeogiyo.payment.entity.PaymentMethod;
import com.georgia.jeogiyo.payment.entity.PaymentStatus;
import com.georgia.jeogiyo.product.dto.request.ProductCreateRequest;
import com.georgia.jeogiyo.product.dto.request.ProductUpdateRequest;
import com.georgia.jeogiyo.product.entity.Product;
import com.georgia.jeogiyo.store.dto.request.StoreCreateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreStatusUpdateRequest;
import com.georgia.jeogiyo.store.dto.request.StoreUpdateRequest;
import com.georgia.jeogiyo.store.entity.Store;
import com.georgia.jeogiyo.store.entity.StoreStatus;
import com.georgia.jeogiyo.user.dto.request.UserSignupRequest;
import com.georgia.jeogiyo.user.entity.Role;
import com.georgia.jeogiyo.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * store/product/ai/payment 서비스 단위 테스트에서 공통으로 쓰는 fixture 입니다.
 *
 * 현재 요청 DTO와 일부 엔티티는 테스트용 생성자나 setter가 충분하지 않기 때문에
 * ReflectionTestUtils로 필요한 필드만 채웁니다. 실제 운영 코드의 캡슐화는 그대로 두고,
 * 테스트에서는 권한/soft delete/AI 이력 같은 비즈니스 흐름에 집중하기 위한 용도입니다.
 *
 * TODO JWT 적용 후 createdBy/updatedBy가 인증 사용자 기준으로 바뀌면
 * audit 기본값 검증도 함께 조정해야 합니다.
 */
public final class DomainTestFixture {

    public static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID OTHER_OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111114");
    public static final UUID CUSTOMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111112");
    public static final UUID MASTER_ID = UUID.fromString("11111111-1111-1111-1111-111111111113");
    public static final UUID OTHER_CUSTOMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111115");

    public static final String OWNER_LOGIN_ID = "owner01";
    public static final String OTHER_OWNER_LOGIN_ID = "owner02";
    public static final String CUSTOMER_LOGIN_ID = "cust01";
    public static final String OTHER_CUSTOMER_LOGIN_ID = "cust02";
    public static final String MASTER_LOGIN_ID = "master01";

    public static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222221");
    public static final UUID STORE_ID = UUID.fromString("33333333-3333-3333-3333-333333333331");
    public static final UUID OTHER_OWNER_STORE_ID = UUID.fromString("33333333-3333-3333-3333-333333333334");
    public static final UUID PRODUCT_ID = UUID.fromString("44444444-4444-4444-4444-444444444441");
    public static final UUID AI_HISTORY_ID = UUID.fromString("55555555-5555-5555-5555-555555555551");

    public static final UUID ORDER_ID = UUID.fromString("66666666-6666-6666-6666-666666666661");
    public static final UUID PAYMENT_ID = UUID.fromString("77777777-7777-7777-7777-777777777771");
    public static final UUID ADDRESS_ID = UUID.fromString("88888888-8888-8888-8888-888888888881");

    public static final String SERVICEABLE_ROAD_ADDRESS = "서울특별시 종로구 새문안로 123";

    private static final LocalDateTime TEST_NOW = LocalDateTime.of(2026, 7, 9, 12, 0);

    private static final PasswordEncoder TEST_PASSWORD_ENCODER = new PasswordEncoder() {
        @Override
        public String encode(CharSequence rawPassword) {
            return "{noop}" + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword.equals(encode(rawPassword));
        }
    };

    private DomainTestFixture() {
    }

    public static User owner() {
        return user(OWNER_ID, OWNER_LOGIN_ID, Role.OWNER);
    }

    public static User otherOwner() {
        return user(OTHER_OWNER_ID, OTHER_OWNER_LOGIN_ID, Role.OWNER);
    }

    public static User customer() {
        return user(CUSTOMER_ID, CUSTOMER_LOGIN_ID, Role.CUSTOMER);
    }

    public static User otherCustomer() {
        return user(OTHER_CUSTOMER_ID, OTHER_CUSTOMER_LOGIN_ID, Role.CUSTOMER);
    }

    public static User master() {
        return user(MASTER_ID, MASTER_LOGIN_ID, Role.MASTER);
    }

    public static User user(UUID userId, String loginId, Role role) {
        UserSignupRequest request = new UserSignupRequest(
                loginId,
                "Test1234!",
                loginId + "Nick",
                "010-0000-0000",
                loginId + "@test.com"
        );

        User user = User.customerCreate(request, TEST_PASSWORD_ENCODER);
        user.changeRole(role);
        ReflectionTestUtils.setField(user, "userId", userId);
        markAudited(user);
        return user;
    }

    public static Category category() {
        Category category = new Category("한식");
        ReflectionTestUtils.setField(category, "categoryId", CATEGORY_ID);
        markAudited(category);
        return category;
    }

    public static Store store(User owner, Category category) {
        Store store = new Store(
                owner,
                category,
                "테스트 가게",
                "서울시 테스트구",
                "02-1111-1111"
        );

        ReflectionTestUtils.setField(store, "storeId", STORE_ID);
        markAudited(store);
        return store;
    }

    public static Address address(User user) {
        AddressCreateRequest request = new AddressCreateRequest(
                SERVICEABLE_ROAD_ADDRESS,
                "101호",
                "03154",
                true
        );

        Address address = Address.create(user, request);
        ReflectionTestUtils.setField(address, "addressId", ADDRESS_ID);
        markAudited(address);
        return address;
    }

    public static Store otherOwnerStore(User otherOwner, Category category) {
        Store store = store(otherOwner, category);
        ReflectionTestUtils.setField(store, "storeId", OTHER_OWNER_STORE_ID);
        return store;
    }

    public static Product product(Store store, Category category) {
        return product(store, category, false);
    }

    public static Product product(Store store, Category category, boolean hidden) {
        Product product = new Product(
                store,
                category,
                "테스트 상품",
                "테스트 상품 설명",
                12000,
                30,
                hidden
        );

        ReflectionTestUtils.setField(product, "productId", PRODUCT_ID);
        markAudited(product);
        return product;
    }

    public static AiHistory successAiHistory(User user, Product product, String requestText, String responseText) {
        AiHistory aiHistory = AiHistory.success(
                user,
                product,
                requestText,
                responseText,
                "gemini-2.5-flash-lite"
        );
        ReflectionTestUtils.setField(aiHistory, "aiHistoryId", AI_HISTORY_ID);
        markAudited(aiHistory);
        return aiHistory;
    }

    public static AiHistory failAiHistory(User user, Product product, String requestText, String errorMessage) {
        AiHistory aiHistory = AiHistory.fail(
                user,
                product,
                requestText,
                "gemini-2.5-flash-lite",
                errorMessage
        );
        ReflectionTestUtils.setField(aiHistory, "aiHistoryId", AI_HISTORY_ID);
        markAudited(aiHistory);
        return aiHistory;
    }

    public static StoreCreateRequest storeCreateRequest(UUID categoryId) {
        StoreCreateRequest request = new StoreCreateRequest();
        ReflectionTestUtils.setField(request, "categoryId", categoryId);
        ReflectionTestUtils.setField(request, "storeName", "테스트 신규 가게");
        ReflectionTestUtils.setField(request, "address", "서울시 테스트구 신규로 10");
        ReflectionTestUtils.setField(request, "phone", "02-9999-9999");
        return request;
    }

    public static StoreUpdateRequest storeUpdateRequest(UUID categoryId) {
        StoreUpdateRequest request = new StoreUpdateRequest();
        ReflectionTestUtils.setField(request, "categoryId", categoryId);
        ReflectionTestUtils.setField(request, "storeName", "테스트 가게 수정");
        ReflectionTestUtils.setField(request, "address", "서울시 테스트구 수정로 20");
        ReflectionTestUtils.setField(request, "phone", "02-2222-2222");
        return request;
    }

    public static StoreStatusUpdateRequest storeStatusRequest(StoreStatus storeStatus) {
        StoreStatusUpdateRequest request = new StoreStatusUpdateRequest();
        ReflectionTestUtils.setField(request, "storeStatus", storeStatus);
        return request;
    }

    public static ProductCreateRequest productCreateRequest(UUID categoryId, boolean useAiDescription) {
        ProductCreateRequest request = new ProductCreateRequest();
        ReflectionTestUtils.setField(request, "categoryId", categoryId);
        ReflectionTestUtils.setField(request, "productName", "테스트 등록 상품");
        ReflectionTestUtils.setField(request, "description", useAiDescription ? null : "직접 입력한 상품 설명");
        ReflectionTestUtils.setField(request, "useAiDescription", useAiDescription);
        ReflectionTestUtils.setField(request, "aiPrompt", useAiDescription ? "상품 설명을 한 문장으로 작성해줘" : null);
        ReflectionTestUtils.setField(request, "price", 12000);
        ReflectionTestUtils.setField(request, "stock", 30);
        ReflectionTestUtils.setField(request, "isHidden", false);
        return request;
    }

    public static ProductUpdateRequest productUpdateRequest(UUID categoryId) {
        ProductUpdateRequest request = new ProductUpdateRequest();
        ReflectionTestUtils.setField(request, "categoryId", categoryId);
        ReflectionTestUtils.setField(request, "productName", "테스트 상품 수정");
        ReflectionTestUtils.setField(request, "description", "수정된 상품 설명");
        ReflectionTestUtils.setField(request, "price", 15000);
        ReflectionTestUtils.setField(request, "stock", 50);
        ReflectionTestUtils.setField(request, "isHidden", false);
        return request;
    }

    public static AiDescriptionRequest aiDescriptionRequest(String requestText) {
        AiDescriptionRequest request = new AiDescriptionRequest();
        ReflectionTestUtils.setField(request, "requestText", requestText);
        return request;
    }

    public static Order order(UUID userId, OrderStatus orderStatus) {
        User user = CUSTOMER_ID.equals(userId)
                ? customer()
                : OTHER_CUSTOMER_ID.equals(userId)
                  ? otherCustomer()
                  : user(userId, "orderUser", Role.CUSTOMER);

        return order(user, orderStatus);
    }

    public static Order order(User user, OrderStatus orderStatus) {

        Category category = category();
        Store store = store(owner(), category);
        Address address = address(user);

        Order order = new Order(
                user,
                store,
                address,
                address.getRoadAddress(),
                address.getDetailAddress(),
                address.getZipcode(),
                18000,
                orderStatus
        );

        ReflectionTestUtils.setField(order, "orderId", ORDER_ID);
        markAudited(order);
        return order;
    }

    public static Payment payment(UUID userId, PaymentStatus paymentStatus) {
        User user = userId.equals(CUSTOMER_ID)
                ? customer()
                : userId.equals(OTHER_CUSTOMER_ID)
                  ? otherCustomer()
                  : user(userId, "paymentUser", Role.CUSTOMER);

        Order order = order(user, OrderStatus.ORDER_REQUESTED);

        Payment payment = new Payment(
                order,
                user,
                PaymentMethod.CARD,
                18000
        );

        ReflectionTestUtils.setField(payment, "paymentId", PAYMENT_ID);

        if (paymentStatus == PaymentStatus.CANCEL) {
            payment.cancel("고객 요청으로 인한 주문 취소");
        } else if (paymentStatus != PaymentStatus.SUCCESS) {
            ReflectionTestUtils.setField(payment, "paymentStatus", paymentStatus);
        }

        markAudited(payment);
        return payment;
    }

    public static PaymentCreateRequest paymentCreateRequest(PaymentMethod paymentMethod) {
        PaymentCreateRequest request = new PaymentCreateRequest();
        ReflectionTestUtils.setField(request, "paymentMethod", paymentMethod);
        return request;
    }

    public static PaymentCancelRequest paymentCancelRequest(String cancelReason) {
        PaymentCancelRequest request = new PaymentCancelRequest();
        ReflectionTestUtils.setField(request, "cancelReason", cancelReason);
        return request;
    }

    public static void markPersisted(Object entity, UUID id) {
        String idFieldName = switch (entity.getClass().getSimpleName()) {
            case "Store" -> "storeId";
            case "Product" -> "productId";
            case "AiHistory" -> "aiHistoryId";
            case "Order" -> "orderId";
            case "Payment" -> "paymentId";
            default -> throw new IllegalArgumentException("지원하지 않는 엔티티입니다.");
        };
        ReflectionTestUtils.setField(entity, idFieldName, id);
        markAudited(entity);
    }

    public static void markAudited(Object entity) {
        ReflectionTestUtils.setField(entity, "createdAt", TEST_NOW);
        ReflectionTestUtils.setField(entity, "createdBy", "user");
        ReflectionTestUtils.setField(entity, "updatedAt", TEST_NOW);
        ReflectionTestUtils.setField(entity, "updatedBy", "user");
    }

    public static AiHistory aiHistory(User owner, Product product) {
        AiHistory aiHistory = AiHistory.success(
                owner,
                product,
                "상품 설명을 작성해줘",
                "AI가 작성한 상품 설명",
                "gemini-2.5-flash-lite"
        );

        markPersisted(aiHistory, AI_HISTORY_ID);
        return aiHistory;
    }
}
