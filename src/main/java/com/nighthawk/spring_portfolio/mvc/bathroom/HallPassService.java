
package com.nighthawk.spring_portfolio.mvc.bathroom;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HallPassService {

    @Autowired
    private TeacherJpaRepository teacherRepository;

    @Autowired
    private HallPassJpaRepository tinkleRepository;

    public Teacher getTeacherByName(String name) {
        String[] names = name.split(" ", 2);
        String firstName = names[0];
        String lastName = names[1];
        List<Teacher> teachers = teacherRepository.findByFirstnameIgnoreCaseAndLastnameIgnoreCase(firstName, lastName);
        return teachers.isEmpty() ? null : teachers.get(0);
    }
    
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll(); // Fetch all teachers
    }
    
    
    public HallPass getActivePassForUser(String username) {
        return tinkleRepository.findByPersonIdAndCheckoutIsNull(username).orElse(null);
    }

    public Teacher getTeacherForActivePass(String firstName, String lastName) {
        List<Teacher> teachers = teacherRepository.findByFirstnameIgnoreCaseAndLastnameIgnoreCase(firstName, lastName);
        return teachers.isEmpty() ? null : teachers.get(0); // make not eq null
    }

    public HallPass requestPass(String teacherName, int period, String activity, String email) {
        if (email != null && teacherName != null) {
            Optional<Teacher> teacherOpt = teacherRepository.getTeacherByName(teacherName);
            
            if (!teacherOpt.isPresent()) {
                return null; // Teacher not found
            }
    
            Teacher teacher = teacherOpt.get(); // Get teacher by ID
    
            HallPass pass = new HallPass();
            pass.setPersonId(email);
            pass.setTeacher_id(teacher.getId()); // Ensure teacher ID is set correctly
            pass.setCheckin(new Date(System.currentTimeMillis()));
            pass.setPeriod(period);
            pass.setActivity(activity);
            
            return tinkleRepository.save(pass);
        }
        return null;
    }
    

    public boolean checkoutPass(String email) {
        
        if (email != null) {
            Optional<HallPass> activePass = tinkleRepository.findByPersonIdAndCheckoutIsNull(email);
            if (activePass.isPresent()) {
                HallPass pass = activePass.get();
                pass.setCheckout(new Date(System.currentTimeMillis()));
                tinkleRepository.save(pass);
                return true;
            }
        }
        return false;
    }
    public HallPass savePass(HallPass pass) {
        return tinkleRepository.save(pass);
    }
}


