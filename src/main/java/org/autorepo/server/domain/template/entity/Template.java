package org.autorepo.server.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.repo.entity.Repo;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "template")
@Entity
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;
    private String title;
    @Enumerated(EnumType.STRING)
    private TemplateType type;
    @Column(columnDefinition = "TEXT")
    private String content;
    @ManyToOne
    @JoinColumn(name = "repo_id")
    private Repo repo;

}
