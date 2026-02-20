package org.nikitakapustkin.security.adapters.in.web.security;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.ports.out.UserRepositoryPort;
import org.nikitakapustkin.security.models.User;
import org.nikitakapustkin.security.models.UserPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepositoryPort userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findUserByLogin(username);

    if (user == null) {
      throw new UsernameNotFoundException("user not found");
    }

    return new UserPrincipal(user);
  }
}
