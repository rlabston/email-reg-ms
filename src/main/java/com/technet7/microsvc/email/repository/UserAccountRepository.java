package com.technet7.microsvc.email.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.technet7.microsvc.email.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsername(String username);

    // After migrating roles into RegisteredEmail.roles, join via the linked RegisteredEmail
    @Query("select u.username as username, r.name as role from UserAccount u join u.registeredEmail re join re.roles r")
    List<UserRoleProjection> findAllUserRoles();
}
