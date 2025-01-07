package com.trident.trident_algo.common.db.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "orderSettings")
@Data
public class OrderSettings extends Audit {

    @Id
    private String id;

    @NotNull(message = "Filter type cannot be blank")
    private FilterType filterType;

    //@NotBlank(message = "Name cannot be blank")
    @Size(max = 15, message = "Symbol must not exceed 15 characters")
    private String symbol;

    @Min(value = 0, message = "Deviation value must be 0 or greater")
    private int deviationValue;

    private enum FilterType {
        PRICE, TIME
    }
}
