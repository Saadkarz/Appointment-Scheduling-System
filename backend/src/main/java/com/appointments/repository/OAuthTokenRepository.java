package com.appointments.repository;

import com.appointments.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    
    Optional<OAuthToken> findByUserIdAndProvider(Long userId, OAuthToken.Provider provider);
    
    void deleteByUserIdAndProvider(Long userId, OAuthToken.Provider provider);
}
