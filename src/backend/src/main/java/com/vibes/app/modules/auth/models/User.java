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

    @Column(nullable = false, unique = true)
    private String username;

    private String profilePictureUrl;

    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserCredentials credentials;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OauthAccounts> oauthAccounts;

    public User() {}

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public UserCredentials getCredentials() { return credentials; }
    public void setCredentials(UserCredentials credentials) { this.credentials = credentials; }
    public List<OauthAccounts> getOauthAccounts() { return oauthAccounts; }
}
