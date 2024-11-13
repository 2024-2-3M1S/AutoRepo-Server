package org.autorepo.server.domain.repo.repository;

import org.autorepo.server.domain.label.entity.Label;
import org.autorepo.server.domain.repo.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoRepository extends JpaRepository<Repo, Long> {
}
