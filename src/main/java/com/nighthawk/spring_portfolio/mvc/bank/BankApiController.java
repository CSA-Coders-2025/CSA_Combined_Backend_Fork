package com.nighthawk.spring_portfolio.mvc.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/bank")
public class BankApiController {

    @Autowired
    private BankService bankService;

    // Request a loan for a bank account
    @PostMapping("/requestLoan")
    public ResponseEntity<String> requestLoan(@RequestBody LoanRequest request) {
        try {
            Bank bank = bankService.requestLoan(request.getUid(), request.getLoanAmount());
            return ResponseEntity.ok("Loan of amount " + request.getLoanAmount() + " granted to user with UID: " + bank.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Loan request failed: " + e.getMessage());
        }
    }

    // Get the loan amount for a bank account
    @GetMapping("/loanAmount")
    public ResponseEntity<Double> getLoanAmount(@RequestParam String uid) {
        Bank bank = bankService.findByUid(uid);
        if (bank != null) {
            return ResponseEntity.ok(bank.getLoanAmount());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // Get a user's gambling profits
    @GetMapping("/gamblingProfits")
    public ResponseEntity<List<Double>> getGamblingProfits(@RequestParam String uid) {
        Bank bank = bankService.findByUid(uid);
        if (bank != null) {
            return ResponseEntity.ok(bank.getGamblingProfit(uid));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
    }

    // Get a user's adventure game profits
    @GetMapping("/adventureGameProfits")
    public ResponseEntity<List<Double>> getAdventureGameProfits(@RequestParam String uid) {
        Bank bank = bankService.findByUid(uid);
        if (bank != null) {
            return ResponseEntity.ok(bank.getAdventureGameProfit(uid));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
    }

    // Get a user's stock profits
    @GetMapping("/stocksProfits")
    public ResponseEntity<List<Double>> getStocksProfits(@RequestParam String uid) {
        Bank bank = bankService.findByUid(uid);
        if (bank != null) {
            return ResponseEntity.ok(bank.getStocksProfit(uid));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ArrayList<>());
    }
}

// Request objects

@Data
@NoArgsConstructor
@AllArgsConstructor
class LoanRequest {
    private String uid;  // Changed from username to uid
    private double loanAmount;
}
