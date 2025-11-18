package com.technet7.microsvc.email.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.technet7.microsvc.email.service.RoleAssignmentService;

/**
 * One-off command runner: if environment variable ASSIGN_ROLE_EMAIL is set, assign the
 * roles from ASSIGN_ROLE_ROLES (comma-separated) to that email and exit.
 *
 * Usage (from project root):
 * ASSIGN_ROLE_EMAIL=rlabston@test.com ASSIGN_ROLE_ROLES=ADMIN ./gradlew bootRun
 */
@Component
public class AssignRoleCommand implements CommandLineRunner {

    private final RoleAssignmentService roleService;

    public AssignRoleCommand(RoleAssignmentService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) throws Exception {
        String email = System.getenv("ASSIGN_ROLE_EMAIL");
        if (email == null || email.isBlank()) {
            return; // nothing to do
        }

        String rolesEnv = System.getenv("ASSIGN_ROLE_ROLES");
        Set<String> roles = new HashSet<>();
        if (rolesEnv == null || rolesEnv.isBlank()) {
            roles.add("ADMIN");
        } else {
            Arrays.stream(rolesEnv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(roles::add);
        }

        System.out.println("AssignRoleCommand: assigning roles " + roles + " to " + email);
        try {
            boolean ok = roleService.assignRolesByEmail(email, roles);
            if (ok) {
                System.out.println("AssignRoleCommand: success");
            } else {
                System.out.println("AssignRoleCommand: email not found: " + email);
            }
        } catch (Exception e) {
            System.err.println("AssignRoleCommand: failed: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            // exit the process after action to avoid leaving the server running
            System.exit(0);
        }
    }
}
