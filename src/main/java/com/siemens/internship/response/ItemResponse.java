package com.siemens.internship.response;

public record ItemResponse(
        Long id,
        String name,
        String description,
        String status,
        String email
) {
}
