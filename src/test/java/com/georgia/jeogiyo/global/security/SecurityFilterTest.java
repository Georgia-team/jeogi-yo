package com.georgia.jeogiyo.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.georgia.jeogiyo.global.jwt.JwtUtil;
import com.georgia.jeogiyo.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();
}
