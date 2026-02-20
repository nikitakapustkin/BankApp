package org.nikitakapustkin.bank.contracts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;

public record CreateUserRequestDto(
    @NotBlank(message = "Login is required") String login,
    @NotBlank(message = "Name is required") String name,
    @Positive(message = "Age must be positive") int age,
    @NotNull(message = "Sex is required") Sex sex,
    @NotNull(message = "Hair color is required") HairColor hairColor) {}
