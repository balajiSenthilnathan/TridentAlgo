package com.trident.trident_algo.common.db.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "group")
@Data
public class Group extends Audit {

    @Id
    private String id;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Type cannot be blank")
    private GroupType type; // Use the enum here

    @NotEmpty(message = "At least one client must be added to the group")
    private List<String> clientIds; // List of client IDs

    private enum GroupType {
        COIN, CLIENT
    }
}
