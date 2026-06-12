package me.code.springboot_postgres.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    /**
     * Legacy role field - kept for backward compatibility during migration.
     * New code should use the `roles` ManyToMany relationship instead.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private LegacyRole legacyRole;

    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "is_protected", nullable = false)
    private boolean isProtected = false;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonIgnoreProperties({"permissions"})
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"seller"})
    private List<Product> sellingProducts;

    public User(String email, String username, String password, LegacyRole legacyRole) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.legacyRole = legacyRole;
    }

    public User(String email, String username, String password, LegacyRole legacyRole, BigDecimal balance, boolean isProtected) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.legacyRole = legacyRole;
        this.balance = balance;
        this.isProtected = isProtected;
    }

    /**
     * Returns authorities based on RBAC permissions from all assigned roles.
     * Each permission name becomes a GrantedAuthority.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            // Add role name as authority (e.g., "ROLE_SUPER_ADMIN")
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            // Add each permission as authority (e.g., "PRODUCT_READ_ALL")
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return authorities;
    }

    /**
     * Get all permission names from all assigned roles.
     */
    public Set<String> getPermissionNames() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has a specific permission.
     */
    public boolean hasPermission(String permissionName) {
        return getPermissionNames().contains(permissionName);
    }

    /**
     * Check if user has any of the given roles by name.
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getName().equals(roleName));
    }

    /**
     * Check if user is a super admin.
     */
    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * Check if user is an admin (any level).
     */
    public boolean isAdmin() {
        return hasRole("SUPER_ADMIN") || hasRole("ADMIN");
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String toString() {
        return "User{id='" + id + ", email='" + email + ", username='" + username + ", roles=" +
                roles.stream().map(Role::getName).collect(Collectors.joining(",")) + "}";
    }

    /**
     * Legacy role enum - kept for backward compatibility with the DB column.
     * Maps: ADMIN -> SUPER_ADMIN or ADMIN in RBAC, USER -> USER in RBAC.
     */
    public enum LegacyRole {
        USER, ADMIN;

        @Override
        public String toString() {
            return this.name();
        }
    }
}
