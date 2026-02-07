package org.nikitakapustkin.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;

    private String login;
    private String name;
    private int age;

    private Sex sex;
    private HairColor hairColor;
}
