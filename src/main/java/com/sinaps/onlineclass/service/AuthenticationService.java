package com.sinaps.onlineclass.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sinaps.onlineclass.dto.UserConverter;
import com.sinaps.onlineclass.dto.UserDto;
import com.sinaps.onlineclass.model.AuthenticationResponse;
import com.sinaps.onlineclass.model.Token;
import com.sinaps.onlineclass.model.User;
import com.sinaps.onlineclass.repository.TokenRepository;
import com.sinaps.onlineclass.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserConverter userConverter;

    private final UserRepository repository;
    private final JwtService jwtService;

    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserDto userDto) {

        // check if user already exist. if exist than authenticate the user
        if(repository.findByUsername(userDto.getUsername()).isPresent()) {
            return new AuthenticationResponse(null, null,"User already exist");
        }

        User user = userConverter.ConvertToEntity(userDto);
        user = repository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        saveUserToken(accessToken, refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken,"User registration was successful");
    }

    public AuthenticationResponse authenticate(UserDto userDto) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                userDto.getUsername(),
                userDto.getPassword()
            )
        );

        User user = repository.findByUsername(userDto.getUsername()).orElseThrow();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllTokenByUser(user);
        saveUserToken(accessToken, refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken, "User login was successful");
    }
    private void revokeAllTokenByUser(User user) {
        List<Token> validTokens = tokenRepository.findAllAccessTokensByUser(user.getId());
        if(validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(t-> {
            t.setLoggedOut(true);
        });

        tokenRepository.saveAll(validTokens);
    }
    private void saveUserToken(String accessToken, String refreshToken, User user) {
        Token token = new Token();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);
        tokenRepository.save(token);
    }

    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // extract the token from authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // extract username from token
        String username = jwtService.extractUsername(token);

        // check if the user exist in database
        User user = repository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("No user found"));

        // check if the token is valid
        if(jwtService.isValidRefreshToken(token, user)) {
            // generate access token
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllTokenByUser(user);
            saveUserToken(accessToken, refreshToken, user);

            return new ResponseEntity(new AuthenticationResponse(accessToken, refreshToken, "New token generated"), HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }
}