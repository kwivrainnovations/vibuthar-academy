package com.academy.project.security;

import com.academy.project.entity.user.User;
import com.academy.project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findActiveByIdentifier(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("No active account found for: " + identifier));
        return new UserPrincipal(user);
    }
}
