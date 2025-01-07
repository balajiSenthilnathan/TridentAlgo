package com.trident.trident_algo.api.controller;

import com.trident.trident_algo.api.service.ClientManagementService;
import com.trident.trident_algo.common.db.entity.BinanceClient;
import com.trident.trident_algo.common.model.GenericPayloadWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.trident.trident_algo.common.helper.PayloadWrapperHelper.handleResponse;

@RestController
@Validated
@RequestMapping("/api/v1/rest/")
@CrossOrigin(origins = "*")
public class ClientManagementAPIController {

    @Autowired
    private ClientManagementService service;

    @GetMapping("/client/list")
    public Mono<ResponseEntity<GenericPayloadWrapper<List<BinanceClient>>>> fetchClientList() {
        return handleResponse(service.fetchAllClients().collectList());
    }

    @PostMapping("/client")
    public Mono<ResponseEntity<GenericPayloadWrapper<BinanceClient>>> createClient(@Valid @RequestBody BinanceClient client) {
        return handleResponse(service.persistClient(client));
    }

    @GetMapping("/client/{id}")
    public Mono<ResponseEntity<GenericPayloadWrapper<BinanceClient>>> fetchClientById(@PathVariable String id) {
        return handleResponse(service.fetchClientById(id));
    }

    @DeleteMapping("/client/{id}")
    public Mono<ResponseEntity<GenericPayloadWrapper<Boolean>>> deleteClientById(@PathVariable String id) {
        return handleResponse(service.deleteClientById(id).thenReturn(true));
    }

}
