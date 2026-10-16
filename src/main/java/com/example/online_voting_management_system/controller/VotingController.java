package com.example.online_voting_management_system.controller;

import com.example.online_voting_management_system.model.Candidate;
import com.example.online_voting_management_system.model.Election;
import com.example.online_voting_management_system.model.User;
import com.example.online_voting_management_system.repository.CandidateRepository;
import com.example.online_voting_management_system.repository.ElectionRepository;
import com.example.online_voting_management_system.repository.UserRepository;
import com.example.online_voting_management_system.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows Frontend index.html connections
public class VotingController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private VoteService voteService;

    // 1. Module 1 & 2: Register Voter / Admin
    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepository.findByVoterId(user.getVoterId()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Voter ID already registered!");
        }
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!", "userId", savedUser.getId()));
    }

    // 2. Module 3 & 4: Login Authentication & Role Details
    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        String voterId = loginData.get("voterId");
        String password = loginData.get("password");

        Optional<User> userOpt = userRepository.findByVoterId(voterId);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            User user = userOpt.get();
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "userId", user.getId(),
                    "voterId", user.getVoterId(),
                    "fullName", user.getFullName(),
                    "role", user.getRole().name()
            ));
        }
        return ResponseEntity.status(401).body("Invalid Voter ID or Password!");
    }

    // 3. Module 5: Create Election (Admin Feature)
    @PostMapping("/elections")
    public ResponseEntity<?> createElection(@RequestBody Election election) {
        Election savedElection = electionRepository.save(election);
        return ResponseEntity.ok(savedElection);
    }

    // Fetch all active elections
    @GetMapping("/elections")
    public List<Election> getAllElections() {
        return electionRepository.findAll();
    }

    // 4. Module 6: Add Candidate to Election (Admin Feature)
    @PostMapping("/candidates")
    public ResponseEntity<?> addCandidate(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String party = (String) payload.get("party");
        Long electionId = Long.parseLong(payload.get("electionId").toString());

        Optional<Election> electionOpt = electionRepository.findById(electionId);
        if (electionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Election not found!");
        }

        Candidate candidate = Candidate.builder()
                .name(name)
                .party(party)
                .election(electionOpt.get())
                .voteCount(0)
                .build();

        return ResponseEntity.ok(candidateRepository.save(candidate));
    }

    // Fetch all candidates (Used by Frontend for Voting Dropdown)
    @GetMapping("/candidates")
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    // Delete Candidate by ID (Admin Feature)
    @DeleteMapping("/candidates/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);

        if (candidateOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error: Candidate not found with ID: " + id));
        }

        candidateRepository.deleteById(id);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Candidate deleted successfully!"
        ));
    }

    // 5. Module 7, 8, 9, 10: Cast Vote (With Encryption & Duplicate Check)
    @PostMapping("/votes/cast")
    public ResponseEntity<?> castVote(@RequestParam Long voterId,
                                      @RequestParam Long electionId,
                                      @RequestParam Long candidateId) {
        try {
            String receiptHash = voteService.castVote(voterId, electionId, candidateId);
            return ResponseEntity.ok(Map.of(
                    "status", "VOTE_SUCCESS",
                    "auditReceiptHash", receiptHash,
                    "message", "Your vote has been securely encrypted and stored!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 6. Module 11 & 12: Live Result Management Tally
    @GetMapping("/results/{electionId}")
    public ResponseEntity<?> getResults(@PathVariable Long electionId) {
        List<Candidate> candidates = candidateRepository.findByElectionId(electionId);
        return ResponseEntity.ok(candidates);
    }
}