package com.technet7.microsvc.email.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final EmailRegistrationRepository emailRepository;

    public JpaUserDetailsService(EmailRegistrationRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Load the registered email by username (which is the email key after migration)
        RegisteredEmail reg = emailRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = reg.getRoles().stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
            .collect(Collectors.toList());

        String effectivePasswordHash = reg.getPasswordHash();

        return org.springframework.security.core.userdetails.User
            .withUsername(reg.getEmail())
            .password(effectivePasswordHash == null ? "" : effectivePasswordHash)
            .authorities(authorities)
            .build();
    }
}
