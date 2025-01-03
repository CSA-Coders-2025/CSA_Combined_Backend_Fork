package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class AssignmentQueue {
    private List<String> working;
    private List<String> waiting;
    private List<String> completed;
    
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // New queues will be initialized as three empty lists.
    public AssignmentQueue() {
        this.working = new ArrayList<>();
        this.waiting = new ArrayList<>();
        this.completed = new ArrayList<>();
    }

    // Reset queue: clears all three lists in queue.
    public void reset() {
        this.working.clear();
        this.waiting.clear();
        this.completed.clear();
    }
}
