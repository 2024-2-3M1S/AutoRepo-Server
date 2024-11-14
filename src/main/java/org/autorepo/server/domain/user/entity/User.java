package org.autorepo.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.global.common.BaseTimeEntity;

import java.util.List;


@NoArgsConstructor
@Table(name = "user")
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String githubId;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    @OneToMany(mappedBy = "user")
    private List<Repo> repos;


}
