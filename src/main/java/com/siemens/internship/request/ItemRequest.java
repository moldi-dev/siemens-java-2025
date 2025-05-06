package com.siemens.internship.request;

import jakarta.validation.constraints.*;

public record ItemRequest(
        @NotBlank(message = "The name is required")
        @Size(min = 1, max = 100, message = "The name must contain at most 100 characters")
        String name,

        @NotBlank(message = "The description is required")
        @Size(min = 1, max = 100, message = "The description must contain at most 100 characters")
        String description,

        @NotBlank(message = "The status is required")
        @Size(min = 1, max = 100, message = "The status must contain at most 100 characters")
        String status,

        // Would've been way simpler to just use the @Email annotation instead the of the pattern, but the requirements ask for a regex
        // The regular expression has been copied from here: https://www.baeldung.com/java-email-validation-regex
        @NotBlank(message = "The email is required")
        @Size(min = 1, max = 100, message = "The email must contain at most 100 characters")
        @Pattern(
                regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
                message = "The email must follow the 'local_part@domain' pattern (e.g: email@domain.com)"
        )
        String email
) {
}
