package com.example.online_voting_management_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Candidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String party;

    @ManyToOne
    @JoinColumn(name = "election_id")
    private Election election;

    private long voteCount = 0;
}