package com.trident.trident_algo.common.helper;

import com.trident.trident_algo.common.model.GenericPayloadWrapper;
import com.trident.trident_algo.common.model.WebErrorWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public class PayloadWrapperHelper {

    public static <T> GenericPayloadWrapper<T> success(T payload) {
        GenericPayloadWrapper<T> responseWrapper = new GenericPayloadWrapper<>();
        responseWrapper.setPayload(payload);
        responseWrapper.setError(null);
        return responseWrapper;
    }

    public static <T> GenericPayloadWrapper<T> error(String message, int code) {
        GenericPayloadWrapper<T> responseWrapper = new GenericPayloadWrapper<>();
        responseWrapper.setPayload(null);
        responseWrapper.setError(new WebErrorWrapper(code, message));
        return responseWrapper;
    }

    public static <T> Mono<ResponseEntity<GenericPayloadWrapper<T>>> handleResponse(Mono<T> serviceCall) {
        return serviceCall
                .map(result -> ResponseEntity.ok(success(result)))
                .onErrorResume(e -> {
                    HttpStatus status = (e instanceof IllegalArgumentException) ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
                    return Mono.just(ResponseEntity.status(status)
                            .body(error(e.getMessage(), status.value())));
                });
    }
}
