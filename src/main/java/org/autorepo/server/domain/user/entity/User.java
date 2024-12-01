package org.autorepo.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.global.common.BaseTimeEntity;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String githubId;
    private String githubToken;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<Repo> repos;


}
