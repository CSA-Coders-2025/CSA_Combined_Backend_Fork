package com.open.spring.mvc.groups;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.open.spring.mvc.assignments.AssignmentSubmission;
import com.open.spring.mvc.person.Person;

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
    /** Automatic unique identifier for Person or group record 
     * --- Id annotation is used to specify the identifier property of the entity.
     * ----GeneratedValue annotation is used to specify the primary key generation
     * strategy to use.
     * ----- The strategy is to have the persistence provider pick an appropriate
     * strategy for the particular database.
     * ----- GenerationType.AUTO is the default generation type and it will pick the
     * strategy based on the used database.
     */
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
