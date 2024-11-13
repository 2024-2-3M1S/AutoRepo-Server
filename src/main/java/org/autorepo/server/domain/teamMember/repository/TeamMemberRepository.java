package org.autorepo.server.domain.teamMember.repository;

import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.domain.teamMember.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
}
