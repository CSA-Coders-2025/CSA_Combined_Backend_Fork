package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.*;

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

    @Autowired
    private StudentSubmissionStatusRepository statusRepo;

    // ---------------- ENUM ----------------
    public enum SubmissionStatus {
        NOT_SUBMITTED,
        SUBMITTED,
        GRADED,
        LATE
    }

    // ---------------- ENTITY ----------------
    @Entity
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class StudentSubmissionStatus {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private Person student;

        @ManyToOne
        private Assignment assignment;

        @Enumerated(EnumType.STRING)
        private SubmissionStatus status;

        public StudentSubmissionStatus(Person student, Assignment assignment, SubmissionStatus status) {
            this.student = student;
            this.assignment = assignment;
            this.status = status;
        }
    }

    // ---------------- REPOSITORY ----------------
    public interface StudentSubmissionStatusRepository extends JpaRepository<StudentSubmissionStatus, Long> {
        List<StudentSubmissionStatus> findByStudent(Person student);
        StudentSubmissionStatus findByStudentAndAssignment(Person student, Assignment assignment);
    }

    // ---------------- DTOs ----------------
    @Getter @Setter
    public static class SubmissionRequestDto {
        public Long assignmentId;
        public List<Long> studentIds;
        public String content;
        public String comment;
        public Boolean isLate;
    }

    // ---------------- API: Submit Assignment ----------------
    @PostMapping("/submit")
    public ResponseEntity<?> submitAssignment(@RequestBody SubmissionRequestDto requestData) {
        Assignment assignment = assignmentRepo.findById(requestData.assignmentId).orElse(null);
        List<Person> students = personRepo.findAllById(requestData.studentIds);

        if (assignment == null) {
            return new ResponseEntity<>(Map.of("error", "Assignment not found"), HttpStatus.NOT_FOUND);
        }

        AssignmentSubmission submission = new AssignmentSubmission(
            assignment, students, requestData.content, requestData.comment, requestData.isLate
        );
        AssignmentSubmission savedSubmission = submissionRepo.save(submission);

        // Set submission status for each student
        for (Person student : students) {
            SubmissionStatus status = requestData.isLate ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED;

            StudentSubmissionStatus existing = statusRepo.findByStudentAndAssignment(student, assignment);
            if (existing != null) {
                existing.setStatus(status);
                statusRepo.save(existing);
            } else {
                statusRepo.save(new StudentSubmissionStatus(student, assignment, status));
            }
        }

        return new ResponseEntity<>(savedSubmission, HttpStatus.CREATED);
    }

    // ---------------- API: Grade Submission ----------------
    @PostMapping("/grade/{submissionId}")
    public ResponseEntity<?> gradeSubmission(
        @PathVariable Long submissionId,
        @RequestParam Double grade,
        @RequestParam(required = false) String feedback
    ) {
        AssignmentSubmission submission = submissionRepo.findById(submissionId).orElse(null);
        if (submission == null) {
            return new ResponseEntity<>(Map.of("error", "Submission not found"), HttpStatus.NOT_FOUND);
        }

        submission.setGrade(grade);
        submission.setFeedback(feedback);
        submissionRepo.save(submission);

        for (Person student : submission.getStudents()) {
            // Update SynergyGrade
            SynergyGrade existing = gradesRepo.findByAssignmentAndStudent(submission.getAssignment(), student);
            if (existing != null) {
                existing.setGrade(grade);
                gradesRepo.save(existing);
            } else {
                gradesRepo.save(new SynergyGrade(grade, submission.getAssignment(), student));
            }

            // Update submission status
            StudentSubmissionStatus status = statusRepo.findByStudentAndAssignment(student, submission.getAssignment());
            if (status != null) {
                status.setStatus(SubmissionStatus.GRADED);
                statusRepo.save(status);
            }
        }

        return new ResponseEntity<>("Grade and status updated", HttpStatus.OK);
    }

    // ---------------- API: Get Status for a Student ----------------
    @GetMapping("/status/{studentId}")
    public ResponseEntity<?> getStudentStatuses(@PathVariable Long studentId) {
        Person student = personRepo.findById(studentId).orElse(null);
        if (student == null) {
            return new ResponseEntity<>(Map.of("error", "Student not found"), HttpStatus.NOT_FOUND);
        }

        List<StudentSubmissionStatus> statuses = statusRepo.findByStudent(student);
        return new ResponseEntity<>(statuses, HttpStatus.OK);
    }
}
