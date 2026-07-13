package com.georgia.jeogiyo.global.jwt;

import com.georgia.jeogiyo.user.entity.Role;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("토큰 생성 시 Bearer 접두어가 붙는다")
    void createTokenHasBearerPrefix() {
        String token = jwtUtil.createToken("test1234", Role.CUSTOMER);

        assertThat(token).startsWith(JwtUtil.BEARER);
    }

    @Test
    @DisplayName("Bearer 접두어가 없는 토큰은 subString 시 예외가 발생한다")
    void subStringTokenThrowsException() {
        assertThatThrownBy(() -> jwtUtil.subStringToken("invalid-token"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("정상 발급된 토큰은 검증을 통과한다")
    void validTokenReturnsTrue() {
        String token = jwtUtil.createToken("test1234", Role.CUSTOMER);
        String pureToken = jwtUtil.subStringToken(token);

        assertThat(jwtUtil.validateToken(pureToken)).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰은 검증에 실패한다")
    void validTokenReturnsFalse() {
        String token = jwtUtil.createToken("test1234", Role.CUSTOMER);
        String pureToken = jwtUtil.subStringToken(token);

        int payloadMiddleIndex = pureToken.indexOf(".") + 5; // header 다음 '.' 이후, payload 중간 지점
        String tampered = pureToken.substring(0, payloadMiddleIndex)
                + "X"
                + pureToken.substring(payloadMiddleIndex + 1);

        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("토큰에서 loginId와 role을 정확히 추출한다")
    void getUserInfoFromToken() {
        String token = jwtUtil.createToken("test1234", Role.OWNER);
        String pureToken = jwtUtil.subStringToken(token);

        Claims claims = jwtUtil.getUserInfoFromToken(pureToken);

        assertThat(claims.getSubject()).isEqualTo("test1234");
        assertThat(claims.get(JwtUtil.AUTHORIZATION_KEY)).isEqualTo("OWNER");
    }

    @Test
    @DisplayName("Swagger관련 Authorization 헤더가 있으면 쿠키보다 헤더를 우선 사용")
    void getTokenFromRequestWithSwagger() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtUtil.AUTHORIZATION_HEADER, "Bearer header-token");
        request.setCookies(new Cookie(JwtUtil.AUTHORIZATION_HEADER, "Bearer%20cookie-token"));

        String result = jwtUtil.getTokenFromRequest(request);

        assertThat(result).isEqualTo("Bearer header-token");
    }

    @Test
    @DisplayName("헤더가 없으면 쿠키 값을 디코딩해서 반환")
    void getTokenFromRequestWithCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(JwtUtil.AUTHORIZATION_HEADER, "Bearer%20cookie-token"));

        String result = jwtUtil.getTokenFromRequest(request);

        assertThat(result).isEqualTo("Bearer cookie-token");
    }
}