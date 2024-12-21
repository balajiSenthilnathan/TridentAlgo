package com.trident.trident_algo.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebErrorWrapper {

    private int code;
    private String message;

}
