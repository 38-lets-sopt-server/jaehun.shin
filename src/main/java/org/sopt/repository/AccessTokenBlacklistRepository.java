package org.sopt.repository;

import org.sopt.domain.AccessTokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenBlacklistRepository extends JpaRepository<AccessTokenBlacklist, Long> {

    boolean existsByToken(String token);
}
