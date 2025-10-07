package com.sample.demo.dto.user;

import com.sample.demo.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatchUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Email(message = "Email must be valid")
    private String email;

    private UserRole role;

    private String firstName;

    private String lastName;
}
