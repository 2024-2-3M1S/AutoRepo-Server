package org.autorepo.server.domain.readme.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.repo.entity.Repo;

@NoArgsConstructor
@Table(name = "readme")
@Entity
public class Readme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long readmeId;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String installation;
    private String stack;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;
}
