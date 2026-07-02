package com.securemail.analyzer.service;

import com.securemail.analyzer.dto.AuthResponse;
import com.securemail.analyzer.dto.RegisterRequest;
import com.securemail.analyzer.entity.User;
import com.securemail.analyzer.enums.Role;
import com.securemail.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.securemail.analyzer.dto.AuthResponse;
import com.securemail.analyzer.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu email adresi zaten kayıtlı.");
        }


        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Email veya şifre hatalı."
                ));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Email veya şifre hatalı."
            );
        }

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Kullanıcı hesabı aktif değil."
            );
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .message("Login başarılı.")
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .build();
    }

}