package com.trident.trident_algo.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

public class BaseOrderAPIService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseOrderAPIService.class);

    private WebClient webClient;

    @Autowired
    public BaseOrderAPIService(WebClient webClient){
        this.webClient = webClient;
    }




}
