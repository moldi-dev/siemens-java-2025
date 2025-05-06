package com.siemens.internship.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpResponse {
    private String timestamp;
    private Integer responseStatusCode;
    private HttpStatus responseStatus;
    private String responseMessage;
    private String responseDeveloperMessage;
    private Object body;
}
