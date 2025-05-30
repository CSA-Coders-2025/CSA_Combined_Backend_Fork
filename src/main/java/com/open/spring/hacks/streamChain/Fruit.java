package com.open.spring.hacks.streamChain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Fruit {
    private String name;
    private Climate climate;

    public String toString() {
        return "Fruit: " + name;
    }
    
}