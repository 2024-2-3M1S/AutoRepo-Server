package org.autorepo.server.domain.template.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.repo.entity.Repo;
import org.autorepo.server.global.common.BaseTimeEntity;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "template")
@Entity
public class Template extends BaseTimeEntity {

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

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
