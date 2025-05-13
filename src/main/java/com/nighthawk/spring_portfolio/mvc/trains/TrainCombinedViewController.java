package com.nighthawk.spring_portfolio.mvc.trains;

import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/mvc/train")
public class TrainCombinedViewController {
    @GetMapping("/home")
    public String getTrainHomePage(){
        return "train/home";
    }
}
