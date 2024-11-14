package org.autorepo.server.domain.template.repository;

import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemplateRepository extends JpaRepository<Template, Long> {
}
