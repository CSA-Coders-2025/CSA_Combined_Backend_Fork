package com.nighthawk.spring_portfolio.mvc.bank;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.UserStocksRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.userStocksTable;

@RestController
@RequestMapping("/api/bank")
public class BankArray {

    @Autowired
    private PersonJpaRepository personRepository;

    @Autowired
    private UserStocksRepository userStocksRepo;

    private Map<String, Double> profitMap = new HashMap<>(); // Stores user profits
    private Map<String, List<BankRequest>> profitHistoryMap = new HashMap<>(); // Stores profit history logs per user

    // DTO for updating profits
    public static class BankRequest {
        private String uid;
        private double amount;
        private String source; // "casino", "stocks", "crypto"

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        @Override
        public String toString() {
            return "Source: " + source + ", Amount: $" + amount;
        }
    }

    @PostMapping("/updateProfit")
    public ResponseEntity<String> updateProfit(@RequestBody BankRequest request) {
        Person user = personRepository.findByUid(request.getUid());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        userStocksTable userStocks = user.getUser_stocks();
        if (userStocks == null) {
            return new ResponseEntity<>("User stocks not found", HttpStatus.NOT_FOUND);
        }

        // Update profit in memory
        profitMap.put(request.getUid(), profitMap.getOrDefault(request.getUid(), 0.0) + request.getAmount());

        // Append profit history
        String profitHistory = userStocks.getCryptoHistory() + request.getSource() + " profit: $" + request.getAmount() + "\n";
        userStocks.setCryptoHistory(profitHistory);
        userStocksRepo.save(userStocks);

        // Store request in profit history map
        profitHistoryMap.putIfAbsent(request.getUid(), new ArrayList<>());
        profitHistoryMap.get(request.getUid()).add(request);

        // Print logs
        System.out.println("Current profitMap: " + profitMap);
        System.out.println("Updated profit history for " + request.getUid() + ": " + profitHistoryMap.get(request.getUid()));

        return new ResponseEntity<>("Profit updated successfully", HttpStatus.OK);
    }

    // Get total profit for a user
    @PostMapping("/getProfit")
    public ResponseEntity<?> getProfit(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        if (!profitMap.containsKey(uid)) {
            return new ResponseEntity<>("No profit data found", HttpStatus.NOT_FOUND);
        }
        System.out.println("Current profitMap: " + profitMap);
        return new ResponseEntity<>("Total Profit: $" + profitMap.get(uid), HttpStatus.OK);
    }

    // Get profit history for a user
    @PostMapping("/getProfitHistory")
    public ResponseEntity<?> getProfitHistory(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        if (!profitHistoryMap.containsKey(uid)) {
            return new ResponseEntity<>("No profit history found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(profitHistoryMap.get(uid), HttpStatus.OK);
    }
}
