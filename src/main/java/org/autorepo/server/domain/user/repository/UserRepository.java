package org.autorepo.server.domain.user.repository;

import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
