package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bathroom/classQueue")
public class BathroomQueueClassController {

    @Autowired
    private BathroomQueueJPARepository bathroomQueueRepository;

    // Mapping of teacher emails to class names
    private static final Map<String, String> TEACHER_CLASS_MAP = new HashMap<>();

    static {
        TEACHER_CLASS_MAP.put("jm1021@gmail.com", "AP Computer Science");
        TEACHER_CLASS_MAP.put("cocraig@powayusd.com", "Honors Medical Interventions");
        TEACHER_CLASS_MAP.put("kozuna@powayusd.com", "Chemistry");
        // Add more mappings as needed
    }

    @GetMapping
    public String getQueuesByClass(Model model) {
        List<BathroomQueue> allQueues = bathroomQueueRepository.findAll();

        // Convert list of queues into a map grouped by class name
        Map<String, List<String>> classQueues = allQueues.stream()
                .collect(Collectors.groupingBy(
                        queue -> TEACHER_CLASS_MAP.getOrDefault(queue.getTeacherEmail(), "Unknown Class"),
                        Collectors.mapping(BathroomQueue::getPeopleQueue, Collectors.toList())
                ));

        model.addAttribute("classQueues", classQueues);
        return "bathroom_queue_class";
    }
}
