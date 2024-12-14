package com.nighthawk.spring_portfolio.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nighthawk.spring_portfolio.mvc.announcement.Announcement;
import com.nighthawk.spring_portfolio.mvc.announcement.AnnouncementJPA;
import com.nighthawk.spring_portfolio.mvc.bathroom.BathroomQueue;
import com.nighthawk.spring_portfolio.mvc.bathroom.BathroomQueueJPARepository;
import com.nighthawk.spring_portfolio.mvc.bathroom.Issue;
import com.nighthawk.spring_portfolio.mvc.bathroom.IssueJPARepository;
import com.nighthawk.spring_portfolio.mvc.bathroom.Teacher;
import com.nighthawk.spring_portfolio.mvc.bathroom.TeacherJpaRepository;
import com.nighthawk.spring_portfolio.mvc.bathroom.Tinkle;
import com.nighthawk.spring_portfolio.mvc.bathroom.TinkleJPARepository;
import com.nighthawk.spring_portfolio.mvc.comment.Comment;
import com.nighthawk.spring_portfolio.mvc.comment.CommentJPA;
import com.nighthawk.spring_portfolio.mvc.jokes.Jokes;
import com.nighthawk.spring_portfolio.mvc.jokes.JokesJpaRepository;
import com.nighthawk.spring_portfolio.mvc.note.Note;
import com.nighthawk.spring_portfolio.mvc.note.NoteJpaRepository;
import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonDetailsService;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.nighthawk.spring_portfolio.mvc.person.PersonRole;
import com.nighthawk.spring_portfolio.mvc.person.PersonRoleJpaRepository;
import com.nighthawk.spring_portfolio.mvc.rpg.question.Question;
import com.nighthawk.spring_portfolio.mvc.rpg.question.QuestionJpaRepository;
import com.nighthawk.spring_portfolio.mvc.user.User;
import com.nighthawk.spring_portfolio.mvc.user.UserJpaRepository;
import com.nighthawk.spring_portfolio.mvc.assignments.Assignment;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentJpaRepository;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmission;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmissionJPA;

@Component
@Configuration // Scans Application for ModelInit Bean, this detects CommandLineRunner
public class ModelInit {  
    @Autowired JokesJpaRepository jokesRepo;
    @Autowired NoteJpaRepository noteRepo;
    @Autowired PersonRoleJpaRepository roleJpaRepository;
    @Autowired PersonDetailsService personDetailsService;
    @Autowired PersonJpaRepository personJpaRepository;
    @Autowired AnnouncementJPA announcementJPA;
    @Autowired CommentJPA CommentJPA;
    @Autowired TinkleJPARepository tinkleJPA;
    @Autowired BathroomQueueJPARepository queueJPA;
    @Autowired TeacherJpaRepository teacherJPARepository;
    @Autowired IssueJPARepository issueJPARepository;
    @Autowired QuestionJpaRepository questionJpaRepository;
    @Autowired UserJpaRepository userJpaRepository;
    @Autowired AssignmentJpaRepository assignmentJpaRepository;
    @Autowired AssignmentSubmissionJPA submissionJPA;

    @Bean
    @Transactional
    CommandLineRunner run() {
        return args -> {
            
            List<Announcement> announcements = Announcement.init();
            for (Announcement announcement : announcements) {
                Announcement announcementFound = announcementJPA.findByAuthor(announcement.getAuthor());  
                if (announcementFound == null) {
                    announcementJPA.save(new Announcement(announcement.getAuthor(), announcement.getTitle(), announcement.getBody(), announcement.getTags())); // JPA save
                }
            }

            List<Comment> Comments = Comment.init();
            for (Comment Comment : Comments) {
                List<Comment> CommentFound = CommentJPA.findByAssignment(Comment.getAssignment()); 
                if (CommentFound.isEmpty()) {
                    CommentJPA.save(new Comment(Comment.getAssignment(), Comment.getAuthor(), Comment.getText())); // JPA save
                }
            }
            
            Question[] questionArray = Question.init();
            for (Question question : questionArray) {
                Question questionFound = questionJpaRepository.findByTitle(question.getTitle());
                if (questionFound == null) {
                    questionJpaRepository.save(new Question(question.getTitle(), question.getContent(), question.getPoints()));
                }
            }

            User[] userArray = User.init();
            for (User user : userArray) {
                List<User> userFound = userJpaRepository.findByUsernameIgnoreCase(user.getUsername()); 
                if (userFound.size() == 0) {
                    userJpaRepository.save(new User(user.getUsername(), user.getPassword(), user.getRole(), user.isEnabled(), user.getBalance(), user.getStonks()));
                }
            }

            String[] jokesArray = Jokes.init();
            for (String joke : jokesArray) {
                List<Jokes> jokeFound = jokesRepo.findByJokeIgnoreCase(joke);  // JPA lookup
                if (jokeFound.size() == 0) {
                    jokesRepo.save(new Jokes(null, joke, 0, 0)); // JPA save
                }
            }

            Person[] personArray = Person.init();
            for (Person person : personArray) {
                List<Person> personFound = personDetailsService.list(person.getName(), person.getEmail());  // lookup
                if (personFound.size() == 0) { 
                    List<PersonRole> updatedRoles = new ArrayList<>();
                    for (PersonRole role : person.getRoles()) {
                        PersonRole roleFound = roleJpaRepository.findByName(role.getName());  // JPA lookup
                        if (roleFound == null) {
                            roleJpaRepository.save(role);  // JPA save
                            roleFound = role;
                        }
                        updatedRoles.add(roleFound);
                    }
                    // Update person with roles from role databasea
                    person.setRoles(updatedRoles); // Object reference is updated

                    personDetailsService.save(person); // JPA save

                    String text = "Test " + person.getEmail();
                    Note n = new Note(text, person);  
                    noteRepo.save(n);  // JPA Save                  
                }
            }

            Tinkle[] tinkleArray = Tinkle.init(personArray);
            for(Tinkle tinkle: tinkleArray)
            {
                // List<Tinkle> tinkleFound = 
                Optional<Tinkle> tinkleFound = tinkleJPA.findByPersonName(tinkle.getPerson_name());
                if(tinkleFound.isEmpty())
                {
                    tinkleJPA.save(tinkle);
                }
            }

            BathroomQueue[] queueArray = BathroomQueue.init();
            for(BathroomQueue queue: queueArray)
            {
                // List<Tinkle> tinkleFound = 
                Optional<BathroomQueue> queueFound = queueJPA.findByTeacherEmail(queue.getTeacherEmail());
                if(queueFound.isEmpty())
                {
                    queueJPA.save(queue);
                }
            }

            // Teacher API is populated with starting announcements
            List<Teacher> teachers = Teacher.init();
            for (Teacher teacher : teachers) {
                List<Teacher> existTeachers = teacherJPARepository.findByFirstnameIgnoreCaseAndLastnameIgnoreCase(teacher.getFirstname(), teacher.getLastname());
                if(existTeachers.isEmpty())
                teacherJPARepository.save(teacher); // JPA save
            }
            // Issue database initialization
            Issue[] issueArray = Issue.init();
            for (Issue issue : issueArray) {
                List<Issue> issueFound = issueJPARepository.findByIssueAndBathroomIgnoreCase(issue.getIssue(), issue.getBathroom());
                if (issueFound.isEmpty()) {
                    issueJPARepository.save(issue);
                }
            }
            // ArrayList<Tinkle> tinkles = new ArrayList<>();
            // for(Person person: personArray)
            // {
            //     tinkles.add(new Tinkle(person, "2"));
            // }
            // for(Tinkle tinkle: tinkles)
            // {
            //     tinkleJPA.save(tinkle);
            // }

            // Assignment database is populated with sample assignments
            Assignment[] assignmentArray = Assignment.init();
            for (Assignment assignment : assignmentArray) {
                Assignment assignmentFound = assignmentJpaRepository.findByName(assignment.getName());
                if (assignmentFound == null) { // if the assignment doesn't exist
                    Assignment newAssignment = new Assignment(assignment.getName(), assignment.getType(), assignment.getDescription(), assignment.getPoints(), assignment.getDueDate());
                    assignmentJpaRepository.save(newAssignment);

                    // create sample submission
                    submissionJPA.save(new AssignmentSubmission(newAssignment, personJpaRepository.findByEmail("madam@gmail.com"), "test submission","test comment"));
                }
            }
        };
    }
}

