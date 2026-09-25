package com.tms.common.dto;

import java.util.List;

public record ErrorResponse(String code, String message, List<FieldErrorDto> fieldErrors) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }
}
