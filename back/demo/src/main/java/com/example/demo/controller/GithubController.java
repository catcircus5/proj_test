package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.repository.PredictionRepository;

import com.example.demo.model.Prediction;

@RestController
@RequestMapping("/webhook")
public class GithubController {

    @Autowired
    private PredictionRepository repo;

    @PostMapping("/github")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {

        // Extract commit message
        String commitMessage = "unknown";

        try {
            var commits = (java.util.List<Map<String, Object>>) payload.get("commits");
            if (commits != null && !commits.isEmpty()) {
                commitMessage = (String) commits.get(0).get("message");
            }
        } catch (Exception e) {
            System.out.println("Error parsing payload");
        }

        // Call ML service
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> request = Map.of(
                "commit_message", commitMessage
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://127.0.0.1:8000/predict",
                request,
                Map.class
        );
        Map body = response.getBody();
        Prediction p = new Prediction();
        p.setCommitMessage(commitMessage);
        p.setWillFail((Boolean) body.get("will_fail"));
        p.setConfidence((Double) body.get("confidence"));
        p.setTimestamp(java.time.LocalDateTime.now());

        repo.save(p);

        System.out.println("Prediction: " + response.getBody());

        return ResponseEntity.ok(response.getBody());
    }
    @GetMapping("/predictions")
    public ResponseEntity<?> getAllPredictions() {
        return ResponseEntity.ok(repo.findAll());
    }
}