package org.autorepo.server.domain.user.repository;

import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGithubId(String githubId);
}
