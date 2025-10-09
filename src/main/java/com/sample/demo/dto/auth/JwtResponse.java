package com.sample.demo.dto.auth;

import com.sample.demo.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private UserRole role;
}