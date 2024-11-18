package com.trident.trident_algo.controller;

import com.trident.trident_algo.model.PositionParams;
import com.trident.trident_algo.service.PositionModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/")
public class PositionModeController {

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
