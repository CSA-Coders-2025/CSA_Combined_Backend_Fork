package com.nighthawk.spring_portfolio.mvc.rpg;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rpg_answer")
@CrossOrigin(origins = {"http://localhost:8085", "http://127.0.0.1:5501"})
public class MarketSentimentController {
    
    // Simple in-memory storage for votes
    private static int bullishVotes = 0;
    private static int bearishVotes = 0;
    private static List<Map<String, Object>> sentimentHistory = new ArrayList<>();

    // Submit a new market sentiment vote
    @PostMapping("/market-sentiment")
    public ResponseEntity<Object> submitSentiment(@RequestBody Map<String, Object> request) {
        String sentiment = (String) request.get("sentiment");
        Long personId = Long.parseLong(request.get("personId").toString());
        String reasoning = (String) request.get("reasoning");

        if (sentiment == null || (!sentiment.equalsIgnoreCase("bullish") && !sentiment.equalsIgnoreCase("bearish"))) {
            return new ResponseEntity<>("Invalid sentiment. Must be 'bullish' or 'bearish'", HttpStatus.BAD_REQUEST);
        }

        // Update vote counts
        if (sentiment.equalsIgnoreCase("bullish")) {
            bullishVotes++;
        } else {
            bearishVotes++;
        }

        // Store the vote with reasoning
        Map<String, Object> vote = new HashMap<>();
        vote.put("personId", personId);
        vote.put("sentiment", sentiment);
        vote.put("reasoning", reasoning);
        vote.put("timestamp", System.currentTimeMillis());
        sentimentHistory.add(vote);

        // Return current statistics
        return new ResponseEntity<>(getStats(), HttpStatus.OK);
    }

    // Get current market sentiment statistics
    @GetMapping("/market-stats")
    public ResponseEntity<Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        int totalVotes = bullishVotes + bearishVotes;
        
        double bullishPercentage = totalVotes > 0 ? (bullishVotes * 100.0) / totalVotes : 0;
        double bearishPercentage = totalVotes > 0 ? (bearishVotes * 100.0) / totalVotes : 0;

        stats.put("bullishPercentage", Math.round(bullishPercentage * 100.0) / 100.0);
        stats.put("bearishPercentage", Math.round(bearishPercentage * 100.0) / 100.0);
        stats.put("totalVotes", totalVotes);
        stats.put("recentVotes", sentimentHistory);

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // Reset votes (for testing purposes)
    @PostMapping("/reset")
    public ResponseEntity<String> resetVotes() {
        bullishVotes = 0;
        bearishVotes = 0;
        sentimentHistory.clear();
        return new ResponseEntity<>("Votes reset successfully", HttpStatus.OK);
    }
}
