package org.autorepo.server.domain.label.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.repo.entity.Repo;

@NoArgsConstructor
@Table(name = "label")
@Entity
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labelId;
    private String name;
    private String labelDescription;
    private String color;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;


}

