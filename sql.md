# Queries to list registered emails with their roles

This file contains example SQL, JPQL and Spring Data JPA queries to obtain registered
emails together with their associated roles. NOTE: this project previously stored
roles in a legacy table named `registered_email_roles`. That legacy table has been
migrated into the canonical role model used by the application: a normalized
`roles` table and a join table `user_role_link` (registered_email_id <-> role_id).

Current model used by the application:
- Table `registered_emails` for the `RegisteredEmail` entity
- Table `roles` (id, name)
- Join table `user_role_link` (registered_email_id, role_id)

---

## 1) Raw SQL — one row per (email, role)

```sql
SELECT re.id,
       re.email,
       re.username,
       ro.name AS role
FROM registered_emails re
LEFT JOIN user_role_link url
  ON url.registered_email_id = re.id
LEFT JOIN roles ro
  ON ro.id = url.role_id
ORDER BY re.id, ro.name;
```

This returns one row for each role a registered email has (emails with multiple roles appear on multiple rows). Useful for processing each role individually.

---

## 2) Raw SQL — aggregate roles into a comma-separated list (one row per email)

MySQL (GROUP_CONCAT):

```sql
SELECT re.id,
       re.email,
       re.username,
       GROUP_CONCAT(DISTINCT ro.name ORDER BY ro.name SEPARATOR ',') AS roles
FROM registered_emails re
LEFT JOIN user_role_link url
  ON url.registered_email_id = re.id
LEFT JOIN roles ro
  ON ro.id = url.role_id
GROUP BY re.id, re.email, re.username
ORDER BY re.id;
```

PostgreSQL (STRING_AGG):

```sql
SELECT re.id,
       re.email,
       re.username,
       STRING_AGG(DISTINCT ro.name, ',' ORDER BY ro.name) AS roles
FROM registered_emails re
LEFT JOIN user_role_link url
  ON url.registered_email_id = re.id
LEFT JOIN roles ro
  ON ro.id = url.role_id
GROUP BY re.id, re.email, re.username
ORDER BY re.id;
```

H2 (if GROUP_CONCAT supported or use LISTAGG depending on version):

```sql
SELECT re.id,
       re.email,
       re.username,
       GROUP_CONCAT(ro.name) AS roles
FROM registered_emails re
LEFT JOIN user_role_link url
  ON url.registered_email_id = re.id
LEFT JOIN roles ro
  ON ro.id = url.role_id
GROUP BY re.id, re.email, re.username;
```

Note: Aggregation syntax/availability differs between DB engines; use the appropriate aggregate function.

---

## 3) JPQL — element-collection join (one row per (entity, role))

```jpql
SELECT r, role
FROM RegisteredEmail r JOIN r.roles role
ORDER BY r.id, role
```

This returns a list of Object[] where element 0 is the RegisteredEmail entity and element 1 is the role (String).

If you prefer only email + role strings:

```jpql
SELECT r.email, role
FROM RegisteredEmail r JOIN r.roles role
ORDER BY r.email, role
```

---

## 4) Spring Data JPA repository examples

Repository interface for simple JPQL returning pairs:

```java
public interface RegisteredEmailRepository extends JpaRepository<RegisteredEmail, Long> {

    @Query("SELECT r.email, role FROM RegisteredEmail r JOIN r.roles role")
    List<Object[]> findEmailsWithRoles();

}
```

Native query example that aggregates roles (MySQL example):

```java
public interface RegisteredEmailRepository extends JpaRepository<RegisteredEmail, Long> {

    @Query(value =
      "SELECT re.id, re.email, re.username, GROUP_CONCAT(rer.role ORDER BY rer.role SEPARATOR ',') AS roles " +
      "FROM registered_emails re " +
      "LEFT JOIN registered_email_roles rer ON rer.registered_email_id = re.id " +
      "GROUP BY re.id, re.email, re.username",
      nativeQuery = true)
    List<Object[]> findAllEmailsWithAggregatedRoles();

}
```

When using the native method above, each returned Object[] will contain (id, email, username, rolesCsv).

---

## 5) Usage notes / recommendations

- If you only need entities in application code, calling `registeredEmailRepository.findAll()` will hydrate `RegisteredEmail` objects with `roles` populated (the entity uses `@ElementCollection(fetch = FetchType.EAGER)` in this project). No custom query is needed for many server-side scenarios.

- For UI or reports where you prefer one row per user with roles combined, use the aggregated SQL (GROUP_CONCAT / STRING_AGG) and parse the CSV into an array in your application code or return it as a JSON array from the DB layer.

- In production, prefer returning structured JSON (roles as arrays) rather than CSV strings; map query results to DTOs or use application-side grouping to produce JSON arrays.

---

## 6) Example: map native query result to DTO (service layer sketch)

```java
public record EmailWithRoles(Long id, String email, String username, List<String> roles) {}

// In service:
List<Object[]> rows = repo.findAllEmailsWithAggregatedRoles();
List<EmailWithRoles> dtos = rows.stream()
    .map(r -> new EmailWithRoles(
        ((Number) r[0]).longValue(),
        (String) r[1],
        (String) r[2],
        r[3] == null ? List.of() : Arrays.asList(((String) r[3]).split(","))))
    .toList();
```

---

If you want, I can add a repository method + a REST endpoint that returns registered emails with roles as a JSON array (one object per user with roles: ["USER","ADMIN"]). Tell me which form (JPQL pairs, aggregated CSV, or JSON DTO) you prefer and I'll implement it.
