package com.example.online_voting_management_system.service;

import com.example.online_voting_management_system.model.Candidate;
import com.example.online_voting_management_system.model.Vote;
import com.example.online_voting_management_system.repository.CandidateRepository;
import com.example.online_voting_management_system.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class VoteService {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public String castVote(Long voterId, Long electionId, Long candidateId) throws Exception {
        // 1. Duplicate Vote Prevention Check
        if (voteRepository.existsByVoterIdAndElectionId(voterId, electionId)) {
            throw new RuntimeException("Duplicate Vote Error: Meeru ee election lo already vote vesaru!");
        }

        // 2. Anonymity Security Encryption (SHA-256 Hash Audit Receipt)
        String rawData = voterId + ":" + electionId + ":" + candidateId + ":" + System.currentTimeMillis();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }

        Vote vote = Vote.builder()
                .voterId(voterId)
                .electionId(electionId)
                .candidateId(candidateId)
                .voteHash(hexString.toString())
                .castAt(LocalDateTime.now())
                .build();

        voteRepository.save(vote);

        // Candidate Vote Count Update
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate kanabadaledu!"));
        candidate.setVoteCount(candidate.getVoteCount() + 1);
        candidateRepository.save(candidate);

        // 3. Dynamic Real-Time WebSocket Push to Frontend (Chart.js Update)
        messagingTemplate.convertAndSend("/topic/results/" + electionId, candidateRepository.findByElectionId(electionId));

        return hexString.toString(); // Security Audit Receipt Hash
    }
}