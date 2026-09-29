package com.pm.authservice.controller;


import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.service.authService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AuthController {
    private final authService AuthService;
    public AuthController(authService AuthService) {
        this.AuthService = AuthService;
    }
    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Optional<String> tokenOp = AuthService.authenticate(loginRequestDTO);

        if(tokenOp.isEmpty()){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = tokenOp.get();
        return new ResponseEntity<>(new LoginResponseDTO(token), HttpStatus.OK);
    }

    @Operation(summary = "Validates jwt token")
    @GetMapping("/validates")
    public ResponseEntity<Void> validateJwtToken(@RequestHeader("Authorization") String authHeader){
        //Authorization -> Bearer<Token> this is the type of header
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return AuthService.validateToken(authHeader.substring(7)) ? ResponseEntity.ok().build()
                :  new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

}
