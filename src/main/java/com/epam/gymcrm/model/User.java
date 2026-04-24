package com.epam.gymcrm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class User {
    @JsonAlias("id")
    private Long userId;

    
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;
    private Boolean isActive;
    private String firstName;
    private String lastName;
    private String username;
}
