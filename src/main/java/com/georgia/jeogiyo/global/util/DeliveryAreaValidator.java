package com.georgia.jeogiyo.global.util;

import com.georgia.jeogiyo.global.exception.BusinessException;
import com.georgia.jeogiyo.global.exception.GlobalErrorCode;
import org.springframework.util.StringUtils;

import java.util.List;

public final class DeliveryAreaValidator {

    private static final String ROAD_ADDRESS_PREFIX = "서울특별시 종로구";

    private static final List<String> DELIVERY_AREA_ROADS = List.of(
            "세종대로",
            "새문안로",
            "종로1길",
            "종로3길",
            "사직로",
            "삼봉로",
            "자하문로",
            "율곡로",
            "우정국로",
            "경희궁길",
            "경희궁1길"
    );

    private DeliveryAreaValidator() {
    }

    public static void validate(String roadAddress) {
        if (!isServiceable(roadAddress)) {
            throw new BusinessException(GlobalErrorCode.OUT_OF_SERVICE_AREA);
        }
    }

    public static boolean isServiceable(String roadAddress) {
        if (!StringUtils.hasText(roadAddress)) {
            return false;
        }

        String normalizedRoadAddress = normalize(roadAddress);
        String normalizedPrefix = normalize(ROAD_ADDRESS_PREFIX);

        if (!normalizedRoadAddress.startsWith(normalizedPrefix)) {
            return false;
        }

        return DELIVERY_AREA_ROADS.stream()
                .map(DeliveryAreaValidator::normalize)
                .anyMatch(normalizedRoadAddress::contains);
    }

    private static String normalize(String value) {
        return value.replace(" ", "");
    }
}