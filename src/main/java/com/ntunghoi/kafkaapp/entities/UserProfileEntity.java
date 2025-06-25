package com.ntunghoi.kafkaapp.entities;

import com.ntunghoi.kafkaapp.models.UserProfile;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Table(name = "user_profiles")
@Entity
public class UserProfileEntity implements UserProfile, UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "preferred_currency", nullable = false)
    private String preferredCurrency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updateDAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_code")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role ->
                new SimpleGrantedAuthority(role.getCode())
        ).collect(Collectors.toSet());
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserProfileEntity setUsername(String username) {
        this.email = username;

        return this;
    }

    public UserProfileEntity setPassword(String password) {
        this.password = password;

        return this;
    }

    public UserProfileEntity setName(String name) {
        this.name = name;

        return this;
    }

    public UserProfileEntity setRoles(Set<RoleEntity> roles) {
        this.roles = roles;

        return this;
    }

    @Override
    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public UserProfileEntity setPreferredCurrency(String preferredCurrency) {
        this.preferredCurrency = preferredCurrency == null ? "USD" : preferredCurrency;

        return this;
    }
}
