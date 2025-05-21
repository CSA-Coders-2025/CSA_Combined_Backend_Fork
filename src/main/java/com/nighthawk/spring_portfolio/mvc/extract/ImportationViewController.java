package com.nighthawk.spring_portfolio.mvc.extract;

import java.util.*;

import lombok.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.nighthawk.spring_portfolio.mvc.person.Person;
import com.nighthawk.spring_portfolio.mvc.person.PersonJpaRepository;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Convert;

@Controller
@RequestMapping("mvc/import")
public class ImportationViewController {
    @Autowired
    private PersonJpaRepository personJpaRepository;

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

    public class PersonThread extends Thread {

        private List<Person> personList;
        private PersonThread previousThread;

        public PersonThread(List<PersonEmpty> personEmptiesSubList, PersonThread previousThread) {
            ArrayList<Person> persons = new ArrayList<Person>(0);
            personEmptiesSubList.stream().forEach(personEmpty -> {
                Person temp = new Person();
                temp.setUid(personEmpty.getUid());
                temp.setPassword(personEmpty.getPassword());
                temp.setEmail(personEmpty.getEmail());
                temp.setName(personEmpty.getName());
                temp.setPfp(personEmpty.getPfp());
                temp.setSid(personEmpty.getSid());
                temp.setBalance("100000");
                temp.setKasmServerNeeded(personEmpty.getKasmServerNeeded());
                temp.setStats(personEmpty.getStats());
                persons.add(temp);
            });
            this.personList = persons;

            this.previousThread = previousThread;

        }

        @Transactional
        private void doTransaction() {
            personJpaRepository.saveAllAndFlush(personList);
        }

        @Override
        public void run() {
            System.out.println("running");
            if (previousThread != null) {
                while (previousThread.isAlive()) {
                    try {
                        System.out.println("Thread: " + String.valueOf(PersonThread.currentThread().getId()) + "is Sleeping");
                        PersonThread.sleep(1000);
                    } catch (Exception e) {
                        // TODO: handle exception
                        System.out.println(e.getStackTrace());
                    }
                }
            }
            this.doTransaction();
        }
    }

    @PostMapping("/person")
    public ResponseEntity<String> importPeopleToDatabase(@RequestBody List<PersonEmpty> body) {
        int entityCount = body.size();
        int batchSize = 10; // how persons are we saving to the database at a time

        PersonThread previousThread = null;
        for (int i = 0; i <= entityCount; i++) {
            if (i > 0) {
                if (i % batchSize == 0) {
                    List<PersonEmpty> subList = body.subList(i - batchSize, i);
                    PersonThread thread = new PersonThread(subList, previousThread);
                    thread.start();
                    previousThread = thread;
                } else if (i == entityCount) {
                    List<PersonEmpty> subList = body.subList(batchSize * (i / batchSize), i);
                    PersonThread thread = new PersonThread(subList, previousThread);
                    thread.start();
                    previousThread = thread;
                }
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}