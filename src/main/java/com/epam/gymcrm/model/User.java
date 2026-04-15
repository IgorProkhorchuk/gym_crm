package com.epam.gymcrm.model;

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
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;
    private Boolean isActive;
}
