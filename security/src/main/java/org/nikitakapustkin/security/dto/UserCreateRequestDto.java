package org.nikitakapustkin.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {
    @NotBlank(message = "Login is required")
    private String login;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Sex is required")
    @Pattern(regexp = "(?i)MALE|FEMALE", message = "Sex must be MALE or FEMALE")
    private String sex;
    @NotBlank(message = "Hair color is required")
    @Pattern(
            regexp = "(?i)BLONDE|BLACK|RED|BROWN|WHITE|GRAY",
            message = "Hair color must be BLONDE, BLACK, RED, BROWN, WHITE or GRAY"
    )
    private String hairColor;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Positive(message = "Age must be positive")
    private int age;
}
