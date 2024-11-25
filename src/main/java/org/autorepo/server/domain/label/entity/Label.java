package org.autorepo.server.domain.label.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.repo.entity.Repo;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "label")
@Entity
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labelId;
    private LabelGenerateType labelGenerateType;
    private String name;
    private String labelDescription;
    private String color;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;


}

