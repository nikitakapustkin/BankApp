package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.SelfValidating;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;

import java.util.UUID;

@Getter
public class ImportUserCommand extends SelfValidating<ImportUserCommand> {

    @NotNull
    private final UUID userId;

    @NotBlank
    private final String login;

    @NotBlank
    private final String name;

    @Min(0)
    private final int age;

    @NotNull
    private final Sex sex;

    @NotNull
    private final HairColor hairColor;

    public ImportUserCommand(UUID userId, String login, String name, int age, Sex sex, HairColor hairColor) {
        this.userId = userId;
        this.login = login;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.hairColor = hairColor;
        validateSelf();
    }
}
