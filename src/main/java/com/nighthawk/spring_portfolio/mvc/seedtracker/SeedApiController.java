package com.nighthawk.spring_portfolio.mvc.seedtracker;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://127.0.0.1:4100")
@RestController
@RequestMapping("/api/seeds")
public class SeedApiController {

    @Autowired
    private SeedJpaRepository seedJpaRepository;

    // Add a new seed entry
    @PostMapping("/")
    public Seed addSeed(@RequestBody Seed seed) {
        // If studentId not provided, auto-increment it
        if (seed.getStudentId() == null) {
            Long maxStudentId = seedJpaRepository.findMaxStudentId().orElse(0L);
            seed.setStudentId(maxStudentId + 1);
        }
        return seedJpaRepository.save(seed);
    }

    // Retrieve all seeds
    @GetMapping("/")
    public List<Seed> getAllSeeds() {
        return seedJpaRepository.findAll();
    }

    // Retrieve seeds by studentId
    @GetMapping("/{studentId}")
    public List<Seed> getSeedsByStudentId(@PathVariable Long studentId) {
        return seedJpaRepository.findByStudentId(studentId);
    }

    // Update a seed's ongoing grade (and/or other fields) by seed "id"
    @PutMapping("/{id}")
    public Seed updateSeed(@PathVariable Long id, @RequestBody Seed updatedSeed) {
        System.out.println("Received request to update seed with id: " + id);

        return seedJpaRepository.findById(id)
            .map(seed -> {
                // Update the grade if new one is provided
                if (updatedSeed.getGrade() != null) {
                    System.out.println("Old Grade: " + seed.getGrade() + ", New Grade: " + updatedSeed.getGrade());
                    seed.setGrade(updatedSeed.getGrade());
                }

                // Update the comment or name if you want to allow that too
                if (updatedSeed.getComment() != null) {
                    seed.setComment(updatedSeed.getComment());
                }
                if (updatedSeed.getName() != null) {
                    seed.setName(updatedSeed.getName());
                }

                return seedJpaRepository.save(seed);
            })
            .orElseThrow(() -> new RuntimeException("Seed not found with id " + id));
    }

    // Delete a seed entry by id
    @DeleteMapping("/{id}")
    public void deleteSeed(@PathVariable Long id) {
        seedJpaRepository.deleteById(id);
    }
}
