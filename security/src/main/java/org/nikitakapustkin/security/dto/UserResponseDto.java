package org.nikitakapustkin.security.dto;

import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
public class UserResponseDto {
    private UUID id;

    private String login;

    private String name;

    private int age;

    private String sex;

    private String hairColor;

    private List<String> friendsLogins;

    private List<AccountResponseDto> accounts;
}
