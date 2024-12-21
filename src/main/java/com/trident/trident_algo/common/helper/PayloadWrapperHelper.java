package com.trident.trident_algo.common.helper;

import com.trident.trident_algo.common.model.GenericPayloadWrapper;
import com.trident.trident_algo.common.model.WebErrorWrapper;

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
}
