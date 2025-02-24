package com.nighthawk.spring_portfolio.mvc.blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Entity
public class Blackjack {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    private String status;

    @Column(nullable = false)
    private double betAmount;

    @Column(columnDefinition = "TEXT")
    private String gameState;

    @Transient
    private Map<String, Object> gameStateMap = new HashMap<>();

    // Initialize deck and shuffle cards
    public void initializeDeck() {
        List<String> deck = generateDeck();
        Collections.shuffle(deck);
        gameStateMap.put("deck", deck);
        persistGameState();
    }

    // Deal initial hands
    public void dealInitialHands() {
        List<String> deck = safeCastToList(gameStateMap.get("deck"));
        List<String> playerHand = new ArrayList<>();
        List<String> dealerHand = new ArrayList<>();

        playerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));

        gameStateMap.put("playerHand", playerHand);
        gameStateMap.put("dealerHand", dealerHand);
        gameStateMap.put("playerScore", calculateScore(playerHand));
        gameStateMap.put("dealerScore", calculateScore(dealerHand));
        persistGameState();
    }

    // Calculate the score of a hand
    public int calculateScore(List<String> hand) {
        int score = 0;
        int aces = 0;

        for (String card : hand) {
            String rank = card.substring(0, card.length() - 1);
            switch (rank) {
                case "A" -> {
                    aces++;
                    score += 11;
                }
                case "K", "Q", "J" -> score += 10;
                default -> score += Integer.parseInt(rank);
            }
        }

        // Adjust score if it exceeds 21 and there are aces
        while (score > 21 && aces > 0) {
            score -= 10;
            aces--;
        }

        return score;
    }

    // Generate a new deck
    private List<String> generateDeck() {
        String[] suits = {"H", "D", "C", "S"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        List<String> deck = new ArrayList<>();

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + suit);
            }
        }

        return deck;
    }

    // Load game state from JSON string
    public void loadGameState() {
        if (this.gameStateMap.isEmpty() && this.gameState != null) {
            this.gameStateMap = fromJsonString(this.gameState);
        }
    }

    // Persist game state as JSON string
    public void persistGameState() {
        this.gameState = toJsonString(this.gameStateMap);
    }

    private String toJsonString(Map<String, Object> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert map to JSON string", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJsonString(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, HashMap.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON string to map", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> safeCastToList(Object obj) {
        if (obj instanceof List) {
            return (List<String>) obj;
        }
        return new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public Map<String, Object> getGameStateMap() {
        return gameStateMap;
    }

    public void setGameStateMap(Map<String, Object> gameStateMap) {
        this.gameStateMap = gameStateMap;
        persistGameState();
    }
}