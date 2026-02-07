package org.nikitakapustkin.security.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.springframework.http.HttpStatus;

import java.net.URI;

@RestController
public class DefaultController {

    @GetMapping(SecurityApiPaths.ROOT)
    public ResponseEntity<Void> home() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/ui/login.html"))
                .build();
    }
}
