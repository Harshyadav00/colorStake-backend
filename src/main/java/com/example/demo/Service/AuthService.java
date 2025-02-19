package com.example.demo.Service;

import com.example.demo.Entity.Dto.LoginResponse;
import com.example.demo.Entity.Dto.RefreshTokenRequest;
import com.example.demo.Entity.Dto.RegisterRequest;
import com.example.demo.Entity.Dto.TokenResponse;
import com.example.demo.Entity.Entity.Account;
import com.example.demo.Entity.Entity.PasswordResetToken;
import com.example.demo.Entity.Entity.User;
import com.example.demo.Exceptions.EmailAlreadyExistsException;
import com.example.demo.Exceptions.InvalidPasswordException;
import com.example.demo.Exceptions.UserNotFoundException;
import com.example.demo.Repository.PasswordResetTokenRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Utility.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    public void register(RegisterRequest user) throws EmailAlreadyExistsException {

            if (userRepository.existsByEmail(user.getEmail())) {
                throw new EmailAlreadyExistsException("Email already exists");
            }
            User newUser = new User();
            newUser.setEmail(user.getEmail());
            newUser.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(newUser);
            accountService.createAccount(user);
    }

    public LoginResponse login(User user) throws UserNotFoundException, InvalidPasswordException {
        try {

            if(!userRepository.existsByEmail(user.getEmail()))
                throw new UserNotFoundException("Invalid Email");

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

            // Generate tokens
            String accessToken = jwtTokenProvider.generateAccessToken(authentication.getName());
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication.getName());

            Account account = accountService.getUserByEmail(user.getEmail()).orElseThrow(() -> new UserNotFoundException("Invalid Email"));


            LoginResponse res = new LoginResponse();
            res.setAccessToken(accessToken);
            res.setRefreshToken(refreshToken);
            res.setId(account.getId());
            res.setEmail(account.getEmail());
            res.setBalance(account.getBalance());
            res.setName(account.getName());

            return res;
        } catch (UserNotFoundException ex) {
            throw ex ;
        } catch (Exception ex) {
            throw new InvalidPasswordException();
        }
    }

    public TokenResponse refresh(RefreshTokenRequest request)  {
//        try {
            // Validate the refresh token
            Claims claims = jwtTokenProvider.validateRefreshToken(request.getRefreshToken());
            String userId = claims.getSubject();

            // Generate new access token
            String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

            return new TokenResponse(newAccessToken, request.getRefreshToken());
//        } catch (Exception ex) {
//            throw new Exception(ex);
//        }

    }

    public void initiatePasswordReset(String email) throws UserNotFoundException {
        Account user = accountService.getUserByEmail(email)
                .orElseThrow(() ->  new UserNotFoundException("Invalid Email") );

        // Check if a token already exists for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);


        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);

        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Password Reset Request",
                "Click the link to reset your password: " + resetUrl);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getUser().getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid user"));
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken); // Remove token after successful reset
    }

}
