package com.trident.trident_algo.common.model;
import com.trident.trident_algo.api.model.BinanceOrderResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericPayloadWrapper<T> {

    private T payload;
    private WebErrorWrapper error;

}
