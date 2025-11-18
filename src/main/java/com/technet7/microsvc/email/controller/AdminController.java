package com.technet7.microsvc.email.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technet7.microsvc.email.dto.RoleAssignmentRequest;
import com.technet7.microsvc.email.dto.RoleAssignmentResponse;
import com.technet7.microsvc.email.dto.UserRoleDto;
import com.technet7.microsvc.email.model.RegisteredEmail;
import com.technet7.microsvc.email.repository.EmailRegistrationRepository;
import com.technet7.microsvc.email.service.RoleAssignmentService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final RoleAssignmentService roleService;
    private final EmailRegistrationRepository emailRepo;

    @Autowired
    public AdminController(RoleAssignmentService roleService, EmailRegistrationRepository emailRepo) {
        this.roleService = roleService;
        this.emailRepo = emailRepo;
    }

    /**
     * Assign roles to a user based on their registered email address.
     * Accessible to ADMIN users (see SecurityConfig).
     */
    @PostMapping("/assign-roles")
    public ResponseEntity<?> assignRoles(@RequestParam String email, @RequestBody RoleAssignmentRequest request) {
        Set<String> roles = request == null ? null : request.getRoles();
        try {
            boolean ok = roleService.assignRolesByEmail(email, roles);
            if (!ok) return ResponseEntity.notFound().build();

            // return updated roles for the user
            // after change: the canonical username/key is the registered email
            RegisteredEmail reg = roleService.getRegisteredEmail(email);
            String usernameKey = reg == null ? email : reg.getEmail();
            java.util.Set<String> assigned = reg == null ? java.util.Set.of() : reg.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());
            return ResponseEntity.ok(new RoleAssignmentResponse(usernameKey, assigned));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Return all users (including their roles). ADMIN only (see SecurityConfig).
     */
    @org.springframework.web.bind.annotation.GetMapping("/users")
    public ResponseEntity<List<RegisteredEmail>> listUsers() {
        List<RegisteredEmail> users = emailRepo.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Return flattened user-role rows as DTOs: { username, role }
     */
    @org.springframework.web.bind.annotation.GetMapping("/user-roles")
    public ResponseEntity<List<UserRoleDto>> listUserRoles() {
        // Build flattened rows from registered_emails and their roles
        List<RegisteredEmail> regs = emailRepo.findAll();
        List<UserRoleDto> dtos = regs.stream()
            .flatMap(re -> re.getRoles().stream().map(role -> new UserRoleDto(re.getEmail(), role.getName())))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
