package com.technet7.microsvc.email.repository;

/**
 * Projection for flattened user-role rows returned by a JPQL join query.
 */
public interface UserRoleProjection {
    String getUsername();
    String getRole();
}
