package com.georgia.jeogiyo.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor
public class CommonResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;


}
