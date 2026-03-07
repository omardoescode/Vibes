package com.vibes.app.modules.auth.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String username;

    private String profilePictureUrl;

    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "user")
    private UserCredentials credentials;

    @OneToMany(mappedBy = "user")
    private List<OauthAccounts> oauthAccounts;
}