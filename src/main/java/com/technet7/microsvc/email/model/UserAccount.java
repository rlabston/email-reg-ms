package com.technet7.microsvc.email.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // Stored password hash
    @Column(nullable = false)
    private String passwordHash;

    // Link to the RegisteredEmail row that this profile belongs to
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registered_email_id", unique = true)
    private RegisteredEmail registeredEmail;

    public UserAccount() {}

    public UserAccount(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Compatibility constructor used by tests that construct a UserAccount with
     * an initial set of role names. We don't resolve roles here (no repo access).
     * The roles are authoritative on the linked RegisteredEmail, so callers
     * typically ensure a RegisteredEmail exists and/or the RoleAssignmentService
     * will link the account to the RegisteredEmail.
     */
    public UserAccount(String username, String passwordHash, java.util.Set<String> roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        // roles are not persisted directly on UserAccount; they come from the
        // associated RegisteredEmail. Keep this constructor for test compatibility.
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public RegisteredEmail getRegisteredEmail() {
        return registeredEmail;
    }

    public void setRegisteredEmail(RegisteredEmail registeredEmail) {
        this.registeredEmail = registeredEmail;
    }

    /**
     * Convenience accessor used by service/tests: returns the set of role names
     * associated with the linked RegisteredEmail. If no RegisteredEmail is
     * linked, returns an empty set.
     */
    public java.util.Set<String> getRoles() {
        if (this.registeredEmail == null || this.registeredEmail.getRoles() == null) {
            return java.util.Set.of();
        }
        return this.registeredEmail.getRoles().stream()
            .map(com.technet7.microsvc.email.model.Role::getName)
            .collect(java.util.stream.Collectors.toSet());
    }
}
