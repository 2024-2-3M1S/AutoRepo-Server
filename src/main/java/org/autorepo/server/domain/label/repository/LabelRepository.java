package org.autorepo.server.domain.label.repository;

import org.autorepo.server.domain.label.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {
}
