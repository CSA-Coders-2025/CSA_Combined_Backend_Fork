package com.nighthawk.spring_portfolio.mvc.assignments;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreRemove;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AssignmentSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Assignment assignment;

    @ManyToMany(cascade = jakarta.persistence.CascadeType.MERGE)
    @JoinTable(
        name = "assignment_submission_students",
        joinColumns = @JoinColumn(name = "submission_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<Person> students = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "assignment_submission_graders",
        joinColumns = @JoinColumn(name = "submission_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private List<Person> assignedGraders;

    private String content;
    private Double grade;
    private String feedback;
    private String comment;
    private long assignmentid;
    private boolean isLate;

    // New field for file upload
    private String filePath;

    @PreRemove
    private void removeStudentsFromSubmission() {
        if (students != null) {
            for (Person student : students) {
                student.getSubmissions().remove(this);
            }
        }
    }

    // Constructor for text-based submission
    public AssignmentSubmission(Assignment assignment, List<Person> students, String content, String comment, boolean isLate) {
        this.assignment = assignment;
        this.students = students;
        this.content = content;
        this.comment = comment;
        this.isLate = isLate;
        this.assignmentid = assignment.getId();
        this.grade = null;
        this.feedback = null;
        this.filePath = null; // No file
    }

    // Constructor for file-based submission
    public AssignmentSubmission(Assignment assignment, List<Person> students, String filePath, String comment, boolean isLate, boolean isFile) {
        this.assignment = assignment;
        this.students = students;
        this.filePath = filePath;
        this.comment = comment;
        this.isLate = isLate;
        this.assignmentid = assignment.getId();
        this.grade = null;
        this.feedback = null;
        this.content = null; // No text content
    }

    // Getters and Setters (if not using Lombok)
    public Long getId() {
        return id;
    }

    public Long getAssignmentId1() {
        return assignmentid;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public List<Person> getStudents() {
        return students;
    }

    public String getContent() {
        return content;
    }

    public String getComment() {
        return comment;
    }

    public String getFeedback() {
        return feedback;
    }

    // Getter for assignment_id (foreign key column)
    public Long getAssignmentId2() {
        return assignment != null ? assignment.getId() : null;
    }

    public Double getGrade() {
        return grade;
    }

    public Boolean getIsLate() {
        return this.isLate;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }
    
}