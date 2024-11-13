package org.autorepo.server.domain.repo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.label.entity.Label;
import org.autorepo.server.domain.readme.entity.Readme;
import org.autorepo.server.domain.template.entity.Template;
import org.autorepo.server.domain.user.entity.User;

import java.util.List;

@NoArgsConstructor
@Table(name = "repo")
@Entity
public class Repo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repoId;
    private String repoName;
    private String repoUrl;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "repo")
    private List<Label> labels;
    @OneToMany(mappedBy = "repo")
    private List<Readme> readmes;
    @OneToMany(mappedBy = "repo")
    private List<Template> templates;
}
