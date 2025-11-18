package com.technet7.microsvc.email.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "registered_emails")
public class RegisteredEmail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    // Stored password hash (do NOT store raw passwords)
    @Column(name = "password")
    private String passwordHash;

    // Roles are now stored in a dedicated roles table with a join table linking
    // registered_emails.id <-> roles.id. This allows role metadata and stable keys.
    @ManyToMany(fetch = FetchType.EAGER, cascade = {jakarta.persistence.CascadeType.MERGE})
    @JoinTable(name = "user_role_link",
        joinColumns = @JoinColumn(name = "registered_email_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<com.technet7.microsvc.email.model.Role> roles = new HashSet<>();
    
    private String registrationDate;

    // Default constructor
    public RegisteredEmail() {
    }

    public RegisteredEmail(String email) {
        this.email = email;
        this.registrationDate = java.time.LocalDateTime.now().toString();
    }

    public RegisteredEmail(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.registrationDate = java.time.LocalDateTime.now().toString();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Convenience overload to allow tests (and callers) to provide role names
     * directly. Each name will be converted to a Role instance. Note that
     * resolving to existing Role rows (by id) is the responsibility of
     * higher-level services / repositories; this helper only creates Role
     * instances with the given names for convenience.
     */
    /**
     * Convenience setter accepting raw role names. Converts each name into a
     * Role instance and stores it in the roles set. This is intended for
     * test convenience and simple setup; resolving to existing persisted Role
     * rows should be done by services/repositories where appropriate.
     */
    public void setRoleNames(java.util.Set<String> roleNames) {
        if (roleNames == null) {
            this.roles = new java.util.HashSet<>();
            return;
        }
        java.util.Set<Role> mapped = roleNames.stream()
            .map(Role::new)
            .collect(java.util.stream.Collectors.toSet());
        this.roles = mapped;
    }
}