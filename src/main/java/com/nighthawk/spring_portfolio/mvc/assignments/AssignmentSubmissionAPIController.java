package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGrade;
import com.nighthawk.spring_portfolio.mvc.synergy.SynergyGradeJpaRepository;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;

/**
 * REST API Controller for managing assignment submissions.
 * Provides endpoints for CRUD operations on assignment submissions.
 */
@RestController
@RequestMapping("/api/submissions")
public class AssignmentSubmissionAPIController {

    @Autowired
    private AssignmentSubmissionJPA submissionRepo;

    @Autowired
    private AssignmentJpaRepository assignmentRepo;

    @Autowired
    private PersonJpaRepository personRepo;

    @Autowired
    private SynergyGradeJpaRepository gradesRepo;

    @Getter
    @Setter
    public static class AssignmentSubmissionReturnDto {
        public Long id;
        public AssignmentReturnDto assignment;
        public List<PersonSubmissionDto> students;
        public String content;
        public String comment;
        public Double grade;
        public String feedback;
        public Boolean isLate;

        public AssignmentSubmissionReturnDto(AssignmentSubmission submission) {
            this.id = submission.getId();
            this.assignment = new AssignmentReturnDto(submission.getAssignment());
            this.students = submission.getStudents().stream().map(PersonSubmissionDto::new).toList();
            this.content = submission.getContent();
            this.comment = submission.getComment();
            this.grade = submission.getGrade();
            this.feedback = submission.getFeedback();
            this.isLate = submission.getIsLate();
        }
    }

    @Getter
    @Setter
    public static class PersonSubmissionDto {
        public Long id;
        public String name;
        public String email;
        public String uid;

        public PersonSubmissionDto(Person person) {
            this.id = person.getId();
            this.name = person.getName();
            this.email = person.getEmail();
            this.uid = person.getUid();
        }
    }
    
    @Getter
    @Setter
    public static class AssignmentReturnDto {
        public Long id;
        public String name;
        public String type;
        public String description;
        public Double points;
        public String dueDate;
        public String timestamp;

        public AssignmentReturnDto(Assignment assignment) {
            this.id = assignment.getId();
            this.name = assignment.getName();
            this.type = assignment.getType();
            this.description = assignment.getDescription();
            this.points = assignment.getPoints();
            this.dueDate = assignment.getDueDate();
            this.timestamp = assignment.getTimestamp();
        }
    }

    /**
     * Get all submissions for a specific student.
     * 
     * @param studentId the ID of the student whose submissions are to be fetched
     * @return a ResponseEntity containing a list of submissions for the given student ID
     */
    @Transactional
    @GetMapping("/getSubmissions/{studentId}")
    public ResponseEntity<?> getSubmissions(@PathVariable Long studentId) {
        List<AssignmentSubmissionReturnDto> submissions = submissionRepo.findByStudentId(studentId).stream()
        .map(AssignmentSubmissionReturnDto::new)
        .toList();;
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }

    /**
     * Create a new assignment submission.
     * 
     * @param submission the AssignmentSubmission object to be created
     * @return a ResponseEntity containing the created submission and HTTP status CREATED
     */
    @PostMapping("/Submit")
    public ResponseEntity<AssignmentSubmission> createAssignment(@RequestBody AssignmentSubmission submission) {
        submissionRepo.save(submission);
        return new ResponseEntity<>(submission, HttpStatus.CREATED);
    }

    @Getter
    @Setter
    public static class SubmissionRequestDto {
        public Long assignmentId;
        public List<Long> studentIds;
        public String content;
        public String comment;
        public Boolean isLate;
    }

    /**
     * Submit an assignment for a student.
     * 
     * @param assignmentId the ID of the assignment being submitted
     * @param studentId    the ID of the student submitting the assignment
     * @param content      the content of the submission
     * @param comment      any comments related to the submission
     * @return a ResponseEntity containing the created submission or an error if the assignment is not found
     */
    @PostMapping("/submit/{assignmentId}")
    public ResponseEntity<?> submitAssignment(
            @RequestBody SubmissionRequestDto requestData
    ) {
        Assignment assignment = assignmentRepo.findById(requestData.assignmentId).orElse(null);
        List<Person> students = personRepo.findAllById(requestData.studentIds);
        if (assignment != null) {
            AssignmentSubmission submission = new AssignmentSubmission(assignment, students, requestData.content, requestData.comment,requestData.isLate);
            AssignmentSubmission savedSubmission = submissionRepo.save(submission);
            return new ResponseEntity<>(new AssignmentSubmissionReturnDto(savedSubmission), HttpStatus.CREATED);
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Assignment not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Grade an existing assignment submission.
     * 
     * @param submissionId the ID of the submission to be graded
     * @param grade        the grade to be assigned to the submission
     * @param feedback     optional feedback for the submission
     * @return a ResponseEntity indicating success or an error if the submission is not found
     */
    @PostMapping("/grade/{submissionId}")
    public ResponseEntity<?> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestParam Double grade,
            @RequestParam(required = false) String feedback
    ) {
        AssignmentSubmission submission = submissionRepo.findById(submissionId).orElse(null);
        if (submission != null) {
            // we have a correct submission
            submission.setGrade(grade);
            submission.setFeedback(feedback);
            submissionRepo.save(submission);

            for (Person student : submission.getStudents()) {
                SynergyGrade assignedGrade = gradesRepo.findByAssignmentAndStudent(submission.getAssignment(), student);
                if (assignedGrade != null) {
                    // the assignment has a previously assigned grade, so we are just updating it
                    assignedGrade.setGrade(grade);
                }
                else {
                    // assignment is not graded, we must create a new grade
                    SynergyGrade newGrade = new SynergyGrade(grade, submission.getAssignment(), student);
                    gradesRepo.save(newGrade);
                }
            }
            

            return new ResponseEntity<>("It worked", HttpStatus.OK);
        }
        Map<String, String> error = new HashMap<>();
        error.put("error", "Submission not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Get all submissions for a specific assignment.
     * 
     * @param assignmentId the ID of the assignment whose submissions are to be fetched
     * @return a ResponseEntity containing a list of submissions or an error if the assignment is not found
     */
    @Transactional
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        Assignment assignment = assignmentRepo.findById(assignmentId).orElse(null);
        if (assignment == null) {
            return new ResponseEntity<>(
                Collections.singletonMap("error", "Assignment not found"), 
                HttpStatus.NOT_FOUND
            );
        }
        List<AssignmentSubmissionReturnDto> submissions = submissionRepo.findByAssignmentId(assignmentId).stream()
            .map(AssignmentSubmissionReturnDto::new)
            .toList();
        return new ResponseEntity<>(submissions, HttpStatus.OK);
    }
}
