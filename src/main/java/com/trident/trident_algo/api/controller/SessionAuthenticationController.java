package com.trident.trident_algo.api.controller;

import com.trident.trident_algo.api.model.BinanceUser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/user/")
public class SessionAuthenticationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthenticationController.class);

    @Autowired
    private com.trident.trident_algo.api.service.UserSessionService userSessionService;

    @GetMapping("/authenticate")
    public ResponseEntity<String> authenticateUser(@RequestBody BinanceUser binanceUser, HttpServletRequest request) {
        userSessionService.saveUserSession(request, binanceUser);
        return ResponseEntity.ok("User Keys saved in Session");
    }

}
