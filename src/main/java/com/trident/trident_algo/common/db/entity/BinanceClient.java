package com.trident.trident_algo.common.db.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@EqualsAndHashCode(callSuper = true)
@Document(collection = "client")
@Data
public class BinanceClient extends Audit{

    @Id
    private String id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be exactly 10 digits")
    private String mobile;

    @Indexed(unique = true)
    @NotBlank(message = "Key cannot be blank")
    @Size(max = 255, message = "Key must not exceed 255 characters")
    private String key;

    @Indexed(unique = true)
    @NotBlank(message = "Secret cannot be blank")
    @Size(max = 255, message = "Secret must not exceed 255 characters")
    private String secret;

    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    @NotNull(message = "Status cannot be blank")
    private ClientStatus status;

    // Enum for the 'type' field
    private enum ClientStatus {
        ACTIVE,
        INACTIVE
    }
}
