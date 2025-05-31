package com.open.spring.mvc.extract;

import java.util.*;

import lombok.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonDetailsService;
import com.open.spring.mvc.person.PersonJpaRepository;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Convert;


@Controller
@RequestMapping("mvc/import")
public class ImportationViewController {
    /////////////////////////////////////////
    /// Autowired Jpa Repositories
    @Autowired
    private PersonJpaRepository personJpaRepository;
    @Autowired
    private PersonDetailsService personDetailsService;

    /////////////////////////////////////////
    /// Export Objects

    // person class based on person table schema (no relationships)
    @Data
    @AllArgsConstructor
    @Convert(attributeName = "person", converter = JsonType.class)
    public static class PersonEmpty {
        private Long id;
        private String uid;
        private String password;
        private String email;
        private String name;
        private String pfp;
        private String sid;
        private Boolean kasmServerNeeded;
        private Map<String, Map<String, Object>> stats;
    }

    /////////////////////////////////////////
    /// Single Imports


    //this import updates a single person, it does not create a new person
    @Transactional
    @PostMapping("person/{id}")
    public ResponseEntity<String> importPersonById(@PathVariable("id") Long id, @RequestBody PersonEmpty person) {
        if (!personJpaRepository.existsById(id)) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
        
        Person personToUpdate = personJpaRepository.findById(id).get();
        //don't allow updating database users
        Person[] defaultPersons = Person.init();
        for(int i=0; i<defaultPersons.length; i++){
            if(defaultPersons[i].getUid().equals(personToUpdate.getUid())){
                return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
            }
        }
       
        boolean samePassword = true;

        if (person.getPassword() != null && !person.getPassword().isBlank()) {
            personToUpdate.setPassword(person.getPassword());
            samePassword = false;
        }
        if (person.getName() != null && !person.getName().isBlank() && !person.getName().equals(personToUpdate.getName())) {
            personToUpdate.setName(person.getName());
        }
        if (person.getEmail() != null && !person.getEmail().isBlank() && !person.getEmail().equals(personToUpdate.getEmail())) {
            personToUpdate.setEmail(person.getEmail());
        }
        if (person.getKasmServerNeeded() != null && !person.getKasmServerNeeded().equals(personToUpdate.getKasmServerNeeded())) {
            personToUpdate.setKasmServerNeeded(person.getKasmServerNeeded());
        }
        if (person.getSid() != null && !person.getSid().equals(personToUpdate.getSid())) {
            personToUpdate.setSid(person.getSid());
        }

        try{
            personDetailsService.save(personToUpdate,samePassword);    
        } catch(Exception e){
            System.out.println(e.getStackTrace());
            return new ResponseEntity<String>(HttpStatus.FAILED_DEPENDENCY);
        }
                
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    //this import creates a single person
    @Transactional
    @PostMapping("person")
    public ResponseEntity<String> importPerson(@RequestBody PersonEmpty personEmpty) {
        
        //don't allow updating database users
        Person[] defaultPersons = Person.init();
        for(int i=0; i<defaultPersons.length; i++){
            if(defaultPersons[i].getUid().equals(personEmpty.getUid())){
                return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
            }
        }
       
        Person temp = new Person();
            temp.setUid(personEmpty.getUid());
            temp.setPassword(personEmpty.getPassword());
            temp.setEmail(personEmpty.getEmail());
            temp.setName(personEmpty.getName());
            temp.setPfp(personEmpty.getPfp());
            temp.setSid(personEmpty.getSid());
            temp.setKasmServerNeeded(personEmpty.getKasmServerNeeded());
            temp.setStats(personEmpty.getStats());

        try{
            personJpaRepository.save(temp);
        } catch(Exception e){
            System.out.println(e.getStackTrace());
            return new ResponseEntity<String>(HttpStatus.FAILED_DEPENDENCY);
        }
                
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }


    /////////////////////////////////////////
    /// Multi Imports (all)

    @Transactional
    private boolean doPersonTransactionFromList(List<Person> personList) {
        try {
            personJpaRepository.saveAllAndFlush(personList);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return false;
        }
        return true;
    }

    private boolean buildBatchAndSave(List<PersonEmpty> personEmptiesSubList) {
        ArrayList<Person> persons = new ArrayList<Person>(0);
        personEmptiesSubList.stream().forEach(personEmpty -> {
            //create an actual person from the personEmpty
            Person temp = new Person();
            temp.setUid(personEmpty.getUid());
            temp.setPassword(personEmpty.getPassword());
            temp.setEmail(personEmpty.getEmail());
            temp.setName(personEmpty.getName());
            temp.setPfp(personEmpty.getPfp());
            temp.setSid(personEmpty.getSid());
            temp.setKasmServerNeeded(personEmpty.getKasmServerNeeded());
            temp.setStats(personEmpty.getStats());
            persons.add(temp); //add it to the list
        });
        return this.doPersonTransactionFromList(persons);
    }

    @PostMapping("all/person")
    public ResponseEntity<String> importPeopleToDatabase(@RequestBody List<PersonEmpty> body) {
        System.out.println("fired");
        int entityCount = body.size();
        int batchSize = 10; // how many persons are we saving to the database at a time

        
        int totalBatches = 0;
        int successfulBatches = 0;
        for (int i = 0; i <= entityCount; i++) {
            if (i > 0) {
                if (i % batchSize == 0) {
                    //if batch size is met, create and save a batch
                    List<PersonEmpty> subList = body.subList(i - batchSize, i);
                    successfulBatches += buildBatchAndSave(subList) ? 1 : 0;
                    totalBatches++;
                } else if (i == entityCount) {
                    //one last batch for any remaining objects
                    List<PersonEmpty> subList = body.subList(batchSize * (i / batchSize), i);
                    successfulBatches += this.buildBatchAndSave(subList) ? 1 : 0;
                    totalBatches++;
                }
            }
        }

        String response = "Attempted import resulted in: " + String.valueOf(successfulBatches)
                + " successful batches of " + String.valueOf(totalBatches) + " attempted batches";
        return new ResponseEntity<String>(response, HttpStatus.OK);
    }
    
}