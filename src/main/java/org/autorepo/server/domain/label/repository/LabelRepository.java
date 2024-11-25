package org.autorepo.server.domain.label.repository;

import org.autorepo.server.domain.label.entity.Label;
import org.autorepo.server.domain.label.entity.LabelGenerateType;
import org.autorepo.server.domain.repo.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, Long> {

    void deleteByLabelGenerateTypeAndRepo(LabelGenerateType labelGenerateType, Repo repo);
}
