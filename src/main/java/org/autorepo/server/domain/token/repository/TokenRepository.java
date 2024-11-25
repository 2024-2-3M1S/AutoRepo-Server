package org.autorepo.server.domain.token.repository;

import org.autorepo.server.domain.token.entity.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, String> {
    Optional<Token> findByRefreshToken(String refreshToken);
}
