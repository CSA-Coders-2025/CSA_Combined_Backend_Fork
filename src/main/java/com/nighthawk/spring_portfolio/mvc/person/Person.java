package com.nighthawk.spring_portfolio.mvc.person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Convert;
import static jakarta.persistence.FetchType.EAGER;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.json.JsonType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * The Person class represents a person entity in the system.
 * It maps to a database table and supports various attributes related to a person.
 * 
 * Lombok annotations:
 * - @Data: Generates getters, setters, toString, equals, hashCode, and a constructor with required arguments.
 * - @AllArgsConstructor: Generates a constructor with all fields as parameters.
 * - @NoArgsConstructor: Generates a constructor with no parameters.
 * - @NonNull: Ensures that the annotated field is non-null.
 * 
 * JPA annotations:
 * - @Entity: Marks this class as a persistent entity.
 * - @Convert: Converts the 'person' field using JsonType (for JSON serialization).
 * - @ManyToMany: Specifies many-to-many relationships with other entities.
 * - @Column: Maps the field to a database column and defines constraints like uniqueness.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Convert(attributeName = "person", converter = JsonType.class)
public class Person {

    /**
     * The unique identifier for each Person.
     * This field is automatically generated by the persistence provider.
     * 
     * @Id: Marks this field as the primary key.
     * @GeneratedValue: Specifies the strategy for generating the primary key (AUTO).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String studentId;
    /**
     * The many-to-many relationship with PersonSections.
     * 
     * @ManyToMany: Indicates a many-to-many relationship with the PersonSections entity.
     * @JoinTable: Specifies the join table to manage the relationship, including the join columns.
     * FetchType.EAGER: Data is fetched immediately when the Person entity is loaded.
     */
    @ManyToMany(fetch = EAGER)
    @JoinTable(
        name = "person_person_sections",  // Name of the join table
        joinColumns = @JoinColumn(name = "person_id"), // Foreign key in the join table pointing to Person
        inverseJoinColumns = @JoinColumn(name = "section_id") // Foreign key pointing to PersonSections
    )
    private Collection<PersonSections> sections = new ArrayList<>();

    /**
     * The many-to-many relationship with PersonRole.
     * 
     * @ManyToMany: Indicates a many-to-many relationship with the PersonRole entity.
     * FetchType.EAGER: Data is eagerly fetched, meaning it is loaded immediately when the Person is loaded.
     * Collection: A generic interface used to store multiple PersonRole objects.
     * ArrayList: The implementation of the List interface.
     */
    @ManyToMany(fetch = EAGER)
    private Collection<PersonRole> roles = new ArrayList<>();

    /**
     * The unique identifier (uid) for the person, used for login.
     * 
     * @NotEmpty: Ensures the field is not null or empty.
     * @Size: Validates that the length of the field is at least 1 character.
     * @Column(unique = true): Marks this field as unique in the database.
     */
    @Column(unique = true)
    @NotEmpty
    private String uid;

    @NotEmpty
    private String email;
    /**
     * The password used for authentication.
     * 
     * @NotEmpty: Ensures the field is not null or empty.
     */
    @NotEmpty
    private String password;

    /**
     * The person's name and date of birth (dob).
     * 
     * @NonNull: Ensures the field is non-null.
     * @Size: Validates that the name length is between 2 and 30 characters.
     * @DateTimeFormat: Specifies the format for the date of birth (yyyy-MM-dd).
     */
    @NonNull
    @Size(min = 2, max = 30, message = "Name (2 to 30 chars)")
    private String name;

    /**
     * Boolean flag indicating whether a Kasm server is needed for the person.
     * The default value is false.
     */
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean kasmServerNeeded = false;

    @Column(nullable = false)
    private String scrumGroup;
    
    /**
     * JSON data to store daily statistics (e.g., calories, steps).
     * 
     * @JdbcTypeCode(SqlTypes.JSON): Specifies the JDBC type code for the JSON data type.
     * @Column(columnDefinition = "jsonb"): Defines the column as a JSONB type in the database.
     * Example JSON structure:
     * "stats": {
     *    "2022-11-13": {
     *        "calories": 2200,
     *        "steps": 8000
     *    }
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Map<String, Object>> stats = new HashMap<>();

    /**
     * Constructor to create a Person object for API calls.
     * This constructor is used to initialize a new person with basic details and a role.
     */
    public Person(String uid, String email, String password, String name, Boolean kasmServerNeeded, String scrumGroup, PersonRole role, String studentId) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.name = name;
        this.kasmServerNeeded = kasmServerNeeded;
        this.scrumGroup = scrumGroup;
        this.roles.add(role);
        this.studentId = studentId;
    }

    /**
     * Helper method to create a Person object with default roles (ROLE_USER, ROLE_STUDENT).
     * 
     * @param name: Name of the person.
     * @param uid: Uid of the person.
     * @param email: Email of the person.
     * @param password: Password for the person.
     * @param kasmServerNeeded: Whether Kasm server is needed.
     * @param Scrum Group of the user
     * @return A new Person object.
     */
    public static Person createPerson(String name, String uid, String email, String password, Boolean kasmServerNeeded, String scrumGroup, String studentId) {
        // By default, Spring Security expects roles to have a "ROLE_" prefix.
        return createPerson(name, uid, email, password, kasmServerNeeded, scrumGroup, password, Arrays.asList("ROLE_USER", "ROLE_STUDENT"),studentId);
    }

    /**
     * Helper method to create a Person object with custom roles.
     * 
     * @param name: Name of the person.
     * @param uid: Uid of the person.
     * @param email: Email of the person.
     * @param password: Password for the person.
     * @param kasmServerNeeded: Whether Kasm server is needed.
     * @param scrumGroup: scrum group of the person
     * @param dob: Date of birth (not used directly).
     * @param roleNames: List of role names to assign to the person.
     * @return A new Person object.
     */
    public static Person createPerson(String name, String uid, String email, String password, Boolean kasmServerNeeded, String scrumGroup, String dob, List<String> roleNames, String studentId) {
        Person person = new Person();
        person.setName(name);
        person.setUid(uid);
        person.setEmail(email);
        person.setPassword(password);
        person.setKasmServerNeeded(kasmServerNeeded);
        person.setScrumGroup(scrumGroup);
        List<PersonRole> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            PersonRole role = new PersonRole(roleName);
            roles.add(role);
        }
        person.setRoles(roles);
        person.setStudentId(studentId);

        return person;
    }
    
    /**
     * Static method to initialize an array of sample Person objects for testing purposes.
     * 
     * @return An array of Person objects.
     */
    public static Person[] init() {
        ArrayList<Person> persons = new ArrayList<>();
        persons.add(createPerson("Thomas Edison", "toby@gmail.com", "toby@gmail.com", "123toby", true, "1", "02-11-1847", Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_TESTER", "ROLE_TEACHER","ROLE_SCRUMLEADER"),"555555"));
        persons.add(createPerson("John Mortensen", "jm1021", "jm1021", "123Qwerty!", false, "1", "10-21-1959", Arrays.asList("ROLE_ADMIN","ROLE_USER", "ROLE_TESTER","ROLE_TEACHER","ROLE_SCRUMLEADER"),"111111"));
        persons.add(createPerson("Nikola Tesla", "niko@gmail.com", "niko@gmail.com", "123niko", true, "1", "07-10-1856", Arrays.asList("ROLE_USER", "ROLE_STUDENT"),"222222"));
        persons.add(createPerson("Madam Curie", "madam@gmail.com", "madam@gmail.com", "123madam", true, "1", "11-07-1867", Arrays.asList("ROLE_USER", "ROLE_STUDENT"),"333333"));
        persons.add(createPerson("Grace Hopper", "hop@gmail.com", "hop@gmail.com", "123hop", true, "1", "12-09-1906", Arrays.asList("ROLE_USER", "ROLE_STUDENT"),"444444"));
        return persons.toArray(Person[]::new);
    }

    /**
     * Main method to print out the details of Person objects.
     * 
     * @param args: Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Obtain Person array from the initializer
        Person[] persons = init();

        // Iterate through each Person and print their details
        for (Person person : persons) {
            System.out.println(person); // Print the Person object details
        }
    }
}
