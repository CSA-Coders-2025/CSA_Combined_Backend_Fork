package com.nighthawk.spring_portfolio.mvc.assignments;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

 import com.nighthawk.spring_portfolio.mvc.person.Person;
 import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
 import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGradeJpaRepository;

@Service
public class SubmissionService {

    @Autowired
     private AssignmentSubmissionJPA submissionRepo;
 
     @Autowired
     private AssignmentJpaRepository assignmentRepo;
 
     @Autowired
     private PersonJpaRepository personRepo;
 
     @Autowired
     private SynergyGradeJpaRepository gradesRepo;

    // Example method to handle the file and other data
    public void submitAssignment(Iterable<Long> studentId, Long assignmentId, String comment, boolean isLate, MultipartFile file) throws Exception {
        // Get the path to the project root and append "/volumes/uploads"
        String projectRoot = System.getProperty("user.dir");  // This gets the project root directory
        String uploadDir = Paths.get(projectRoot, "volumes", "uploads").toString();  // Points to the "uploads" folder under "volumes"

        // Create the directory if it doesn't exist
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);  // Creates the "uploads" directory if it doesn't exist
        }

        // Handle file name conflict by appending a timestamp or UUID (optional)
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Define the full path to save the file
        String filePath = Paths.get(uploadDir, fileName).toString();

        // Save the file locally
        try {
            file.transferTo(new File(filePath));  // Save the file to the specified location
        } catch (IOException e) {
            throw new Exception("Error saving file: " + e.getMessage());
        }

        String relativeFilePath = filePath.replace(projectRoot + File.separator, "");

        Assignment assignment = assignmentRepo.findById(assignmentId).orElse(null);
        List<Person> students = personRepo.findAllById(studentId);

        if (assignment != null) {
             AssignmentSubmission submission = new AssignmentSubmission(assignment, students, relativeFilePath, comment, isLate, true);
             AssignmentSubmission savedSubmission = submissionRepo.save(submission);
        }

        // Logic to store other details (studentId, assignmentId, etc.) to the database
        // Example: You can save the file path in your database, along with the other details
    }
}
