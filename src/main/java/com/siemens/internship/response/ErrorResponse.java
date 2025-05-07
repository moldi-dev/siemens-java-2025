package com.siemens.internship.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String timestamp;
    private Integer errorCode;
    private HttpStatus errorStatus;
    private String errorMessage;
    private String requestPath;
    private Map<?, ?> validationErrors;
}
