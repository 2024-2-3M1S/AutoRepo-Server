package org.autorepo.server.domain.template.repository;

import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.template.entity.Template;
import org.autorepo.server.domain.template.entity.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    Optional<Template> findByRepoAndType(Repo repo, TemplateType type);

    List<Template> findAllByType(TemplateType type);

    List<Template> findAllByRepoIn(List<Repo> userRepos);
}
