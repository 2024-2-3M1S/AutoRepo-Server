package org.autorepo.server.domain.readme.repository;

import org.autorepo.server.domain.label.entity.Label;
import org.autorepo.server.domain.readme.entity.Readme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadmeRepository extends JpaRepository<Readme, Long> {
}
