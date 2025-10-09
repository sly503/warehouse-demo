package com.sample.demo.service;

import com.sample.demo.dto.auth.ForgotPasswordRequest;
import com.sample.demo.dto.auth.ResetPasswordRequest;
import com.sample.demo.exception.BadRequestException;
import com.sample.demo.exception.ResourceNotFoundException;
import com.sample.demo.model.entity.PasswordResetToken;
import com.sample.demo.model.entity.User;
import com.sample.demo.repository.PasswordResetTokenRepository;
import com.sample.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int EXPIRY_HOURS = 24;

    @Transactional
    public String createPasswordResetToken(ForgotPasswordRequest request) {
        log.info("Processing password reset request for email: {}", request.getEmail());

        User user = userRepository.findByUsername(request.getEmail())
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getEmail().equals(request.getEmail()))
                        .findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(EXPIRY_HOURS));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        log.info("Password reset token created for user: {}", user.getUsername());

        // In production, you would send this token via email
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset with token");

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successfully for user: {}", user.getUsername());
    }
}
