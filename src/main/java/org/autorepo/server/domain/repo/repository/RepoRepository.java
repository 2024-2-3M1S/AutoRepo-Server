package org.autorepo.server.domain.repo.repository;

import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RepoRepository extends JpaRepository<Repo, Long> {
    Optional<Repo> findByRepoUrl(String repoUrl);

    List<Repo> findAllByUser(User user);
}
