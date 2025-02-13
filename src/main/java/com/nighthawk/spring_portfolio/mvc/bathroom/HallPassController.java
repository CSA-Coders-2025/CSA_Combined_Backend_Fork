package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;

@RestController
@RequestMapping("/api/hallpass")
public class HallPassController {

    private static final Logger logger = LoggerFactory.getLogger(HallPassController.class);

    @Autowired private HallPassService hallPassService;
    @Getter
    public static class HallPassRequestDTO
    {
        private String userName;
        private Long teacherId;
        private String teacherName;
        private int period;
        private String activity;

        @Override
        public String toString() {
            return "HallPassRequestDTO{" +
                    "userName='" + userName + '\'' +
                    ", teacherId=" + teacherId +
                    ", teacherName=" + teacherName +
                    ", period=" + period +
                    ", activity='" + activity + '\'' +
                    '}';
        }
        
    }
    /**
     * Endpoint to request a hall pass by providing the user's email address.
     */
    //@CrossOrigin(origins = "http://127.0.0.1:4100")
    @PostMapping("/request")
    public ResponseEntity<Object> requestHallPass(@RequestBody HallPassRequestDTO request) {
        try {
            HallPass pass = hallPassService.requestPass(
                request.getTeacherId(),  
                request.getPeriod(),
                request.getActivity(),
                request.getUserName()
            );
            logger.info("Hall pass issued successfully: {}", pass);
            return ResponseEntity.ok(pass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    

    /**
     * Endpoint to check out (return) a hall pass.
     */
    //@CrossOrigin(origins = "http://127.0.0.1:4100")
    @PostMapping("/checkout")
    public ResponseEntity<Object> checkoutHallPass(@RequestParam("email") String emailAddress) {
        try {
            boolean pass = hallPassService.checkoutPass(emailAddress);
            return ResponseEntity.ok(pass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    @CrossOrigin(origins = "http://127.0.0.1:8080")
    @GetMapping("/getTeacher")
    public ResponseEntity<Object> getTeacher(@RequestParam("fname") String firstName, 
    @RequestParam("lname") String lastName) {
        try {
            Teacher teacher = hallPassService.getTeacherByName(firstName, lastName);
            return ResponseEntity.ok(teacher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    //@CrossOrigin(origins = "http://127.0.0.1:4100")
    @GetMapping("/getactivepass")
    public ResponseEntity<Object> getPass(@RequestParam("email") String emailAddress) {
        try {
            HallPass hallpass = hallPassService.getActivePassForUser(emailAddress);
            return ResponseEntity.ok(hallpass);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "http://127.0.0.1:8080")
@GetMapping("/getTeachers")
public ResponseEntity<Object> getAllTeachers() {
    try {
        List<Teacher> teachers = hallPassService.getAllTeachers();
        return ResponseEntity.ok(teachers);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error: " + e.getMessage());
    }
}
}
