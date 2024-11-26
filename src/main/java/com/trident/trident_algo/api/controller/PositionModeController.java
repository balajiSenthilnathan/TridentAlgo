package com.trident.trident_algo.api.controller;

import com.trident.trident_algo.api.model.PositionParams;
import com.trident.trident_algo.api.service.PositionModeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/")
public class PositionModeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PositionModeController.class);

    @Autowired
    private PositionModeService positionModeService;

    @GetMapping("/enableHedgeMode")
    public ResponseEntity<Mono<String>> enableHedgeMode(@RequestParam Boolean enableFlag) throws Exception {
        Mono<String> response = positionModeService.enableHedgeMode(enableFlag);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/openPosition")
    public Mono<ResponseEntity<String>> openPositionByMode(@RequestBody PositionParams positionParams) throws Exception {
        return positionModeService.openPositionByMode(positionParams)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(e.getMessage())));
    }

}
