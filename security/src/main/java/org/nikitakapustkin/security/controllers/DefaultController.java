package org.nikitakapustkin.security.controllers;

import java.net.URI;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

  @GetMapping(SecurityApiPaths.ROOT)
  public ResponseEntity<Void> home() {
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/ui/login.html")).build();
  }
}
