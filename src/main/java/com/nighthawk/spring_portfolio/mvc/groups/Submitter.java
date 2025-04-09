package com.nighthawk.spring_portfolio.mvc.groups;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nighthawk.spring_portfolio.mvc.assignments.AssignmentSubmission;
import com.nighthawk.spring_portfolio.mvc.person.Person;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Person.class, name = "person"),
    @JsonSubTypes.Type(value = Groups.class, name = "group")
})
@Getter
public abstract class Submitter {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "submitter", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<AssignmentSubmission> submissions;

    public List<Person> getMembers() {
        if (this instanceof Groups) {
            return ((Groups) this).getGroupMembers();
        }
        return List.of((Person) this);
    }
}
