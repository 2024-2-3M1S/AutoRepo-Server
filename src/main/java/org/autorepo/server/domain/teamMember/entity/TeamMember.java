package org.autorepo.server.domain.teamMember.entity;

import jakarta.persistence.*;
import lombok.*;
import org.autorepo.server.domain.readme.entity.Readme;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String memberName;
    private String memberRole;
    private String profileUrl;

    @ManyToOne
    @JoinColumn(name = "readme_id")
    private Readme readme;


}
