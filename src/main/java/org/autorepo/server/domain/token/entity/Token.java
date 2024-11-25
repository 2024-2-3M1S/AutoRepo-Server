package org.autorepo.server.domain.token.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Token")
public class Token {
    @Id
    private Long userId;
    private String refreshToken;
}
