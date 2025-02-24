package com.nighthawk.spring_portfolio.mvc.cryptoMining;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class MiningUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_id", unique = true)
    private Person person;

    private double btcBalance = 0.0;
    private double pendingBalance = 0.0;
    private boolean isMining = false;
    private String currentPool = "nicehash";
    private int shares = 0;
    private double currentHashrate = 0.0;
    private double dailyRevenue;
    private double powerCost;
    
    @ManyToMany
    private List<GPU> ownedGPUs = new ArrayList<>();

    @ManyToMany
    private List<GPU> activeGPUs = new ArrayList<>();

    public MiningUser(Person person) {
        this.person = person;
        this.btcBalance = 0.0;
        this.pendingBalance = 0.0;
        this.shares = 0;
        this.isMining = false;
        this.currentPool = "default";
    }

    // Add this explicit getter for the controller
    public List<GPU> getGpus() {
        return this.ownedGPUs;
    }

    public void addGPU(GPU gpu) {
        if (ownedGPUs == null) {
            ownedGPUs = new ArrayList<>();
        }
        if (activeGPUs == null) {
            activeGPUs = new ArrayList<>();
        }
        ownedGPUs.add(gpu);
        activeGPUs.add(gpu);
        updateHashrate();
    }

    private void updateHashrate() {
        this.currentHashrate = activeGPUs.stream()
            .mapToDouble(GPU::getHashRate)
            .sum();
    }

    public double getPowerConsumption() {
        return ownedGPUs.stream()
            .mapToInt(GPU::getPowerConsumption)
            .sum();
    }

    public double getAverageTemperature() {
        return ownedGPUs.stream()
            .mapToInt(GPU::getTemp)
            .average()
            .orElse(0);
    }

    public double getCurrentHashrate() {
        if (!isMining) return 0.0;
        return activeGPUs.stream()
            .mapToDouble(GPU::getHashRate)
            .sum();
    }

    public boolean ownsGPU(GPU gpu) {
        return this.ownedGPUs.contains(gpu);
    }

    public boolean ownsGPUById(Long gpuId) {
        return this.ownedGPUs.stream()
            .anyMatch(gpu -> gpu.getId().equals(gpuId));
    }

    public boolean toggleGPU(GPU gpu) {
        if (!ownedGPUs.contains(gpu)) {
            throw new RuntimeException("You don't own this GPU");
        }

        boolean isActive = activeGPUs.contains(gpu);
        if (isActive) {
            activeGPUs.remove(gpu);
        } else {
            activeGPUs.add(gpu);
        }
        updateHashrate();
        return !isActive; // returns new state
    }

    public boolean isGPUActive(GPU gpu) {
        return activeGPUs.contains(gpu);
    }

    public List<GPU> getActiveGPUs() {
        return this.activeGPUs;
    }

    public void setMining(boolean mining) {
        this.isMining = mining;
        if (!mining) {
            this.currentHashrate = 0.0;
        } else {
            updateHashrate();
        }
    }
    

    public double getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(double dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public double getPowerCost() {
        return powerCost;
    }

    public void setPowerCost(double powerCost) {
        this.powerCost = powerCost;
    }

    public List<GPU> getOwnedGPUs() {
        return this.ownedGPUs;
    } 
}