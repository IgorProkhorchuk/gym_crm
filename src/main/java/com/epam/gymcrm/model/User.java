package com.epam.gymcrm.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {
    @ToString.Include
    @JsonAlias("id")
    private Long userId;

    private String password;
    private Boolean active;
    private String firstName;
    private String lastName;
    @EqualsAndHashCode.Include
    private String username;
}
