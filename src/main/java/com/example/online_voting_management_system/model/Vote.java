package com.example.online_voting_management_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"voter_id", "election_id"}) // Database level Duplicate Vote Prevention
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Column(name = "election_id", nullable = false)
    private Long electionId;

    @Column(nullable = false)
    private Long candidateId;

    private String voteHash; // Security Encryption & Anonymity
    private LocalDateTime castAt;
}