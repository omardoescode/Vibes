package com.vibes.app.modules.auth.repositories;

import com.vibes.app.modules.auth.models.OauthAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OauthAccountsRepository extends JpaRepository<OauthAccounts, Long> {
  Optional<OauthAccounts> findByProviderAndProviderUserId(String provider, String providerUserId);

  List<OauthAccounts> findByUserId(UUID userId);

  boolean existsByProviderAndProviderUserId(String provider, String providerUserId);
}
