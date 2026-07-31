package com.netpoint.main.dto;

import com.netpoint.main.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDTO(
        Integer id,

        @NotBlank(message = "Name is required")
//        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String role,
        User.AccountStatus status,
        String profileImage
) {
}
