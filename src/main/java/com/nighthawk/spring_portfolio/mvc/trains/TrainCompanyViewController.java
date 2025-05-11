package com.nighthawk.spring_portfolio.mvc.trains;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonDetailsService;

@Controller
@RequestMapping("/mvc/train")
public class TrainCompanyViewController {
    @Autowired
    private PersonDetailsService personRepository;

    @Autowired
    private TrainJPARepository trainRepository;

    @Autowired
    private TrainCompanyJPARepository repository;

    @GetMapping("/home")
    public String getTrainHomePage(){
        return "train/home";
    }


    //idk why this one only works with transactional annotation, its probally to do with the one-to-one relationship betweeen TrainCompany and Person
    @GetMapping("/get/company")
    @Transactional
    public ResponseEntity<TrainCompany> getCompany(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            // if a train company doesn't exist, then make one
            TrainCompany company = new TrainCompany();
            company.setCompanyName("Company " + id.toString());
            company.setOwner(person);

            repository.save(company);
        }

        TrainCompany company = repository.getById(id);
        
        ResponseEntity<TrainCompany> responseEntity = new ResponseEntity<TrainCompany>(company, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping("/get/trains")
    public ResponseEntity<List<Train>> getTrains(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Person person = personRepository.getByUid(userDetails.getUsername());
        Long id = person.getId();

        if (!repository.existsById(id)) {
            ResponseEntity<List<Train>> responseEntity = new ResponseEntity<List<Train>>(HttpStatus.FAILED_DEPENDENCY);
            return responseEntity;
        }

        TrainCompany company = repository.getById(id);

        if(!trainRepository.existsByCompanyId(company.getId())){
            Train train = new Train();
            train.setCompany(company);
            train.setPosition(Float.valueOf(0));
            trainRepository.save(train);
        }
        
        List<Train> trains = trainRepository.getAllByCompanyId(company.getId());

        ResponseEntity<List<Train>> responseEntity = new ResponseEntity<List<Train>>(trains, HttpStatus.OK);
        return responseEntity;
    }
}
