package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

@RestController
@RequestMapping("/api/queue")
public class BathroomQueueApiController {

    @Autowired
    private BathroomQueueJPARepository repository;

    // DTO (Data Transfer Object) class for queue entries
    @Getter
    public static class QueueDto {
        private String teacherEmail;
        private String studentName;
    }

    // Add a student to the queue for a specific teacher
    @PostMapping("/add")
    public ResponseEntity<Object> addToQueue(@RequestBody QueueDto queueDto) {
        Optional<BathroomQueue> existingQueue = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (existingQueue.isPresent()) {
            existingQueue.get().addStudent(queueDto.getStudentName());
            repository.save(existingQueue.get());
        } else {
            BathroomQueue newQueue = new BathroomQueue(queueDto.getTeacherEmail(), queueDto.getStudentName());
            repository.save(newQueue);
        }
        return new ResponseEntity<>(queueDto.getStudentName() + " was added to " + queueDto.getTeacherEmail(), HttpStatus.CREATED);
    }

    // Remove a student from the queue for a specific teacher
    @CrossOrigin(origins = "http://127.0.0.1:4100")
    @DeleteMapping("/remove")
    public ResponseEntity<Object> removeFromQueue(@RequestBody QueueDto queueDto) {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            BathroomQueue bathroomQueue = queueEntry.get();
            try {
                bathroomQueue.removeStudent(queueDto.getStudentName());
                repository.save(bathroomQueue);
                return new ResponseEntity<>("Removed " + queueDto.getStudentName() + " from " + queueDto.getTeacherEmail() + "'s queue", HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    // Approve the student at the front of the queue
    @CrossOrigin(origins = "http://127.0.0.1:4100")
    @PostMapping("/approve")
    public ResponseEntity<Object> approveStudent(@RequestBody QueueDto queueDto) {
        Optional<BathroomQueue> queueEntry = repository.findByTeacherEmail(queueDto.getTeacherEmail());
        if (queueEntry.isPresent()) {
            BathroomQueue bathroomQueue = queueEntry.get();
            String frontStudent = bathroomQueue.getFrontStudent();
            if (frontStudent != null && frontStudent.equals(queueDto.getStudentName())) {
                bathroomQueue.approveStudent();
                repository.save(bathroomQueue);
                return new ResponseEntity<>("Approved " + queueDto.getStudentName(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Student is not at the front of the queue", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>("Queue for " + queueDto.getTeacherEmail() + " not found", HttpStatus.NOT_FOUND);
    }

    // Retrieve all queues (for all teachers)
    @GetMapping("/all")
    public ResponseEntity<List<BathroomQueue>> getAllQueues() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }

    // Retrieve active queues (with Cross-Origin support)
    @CrossOrigin(origins = "http://localhost:8085")
    @GetMapping("/getActive")
    public ResponseEntity<Object> getActiveQueues() {
        return new ResponseEntity<>(repository.findAll(), HttpStatus.OK);
    }
}