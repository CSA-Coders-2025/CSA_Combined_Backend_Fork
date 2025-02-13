package com.nighthawk.spring_portfolio.mvc.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.*;

@Entity
@Data  // Annotations to simplify writing code (ie constructors, setters)
@NoArgsConstructor
@AllArgsConstructor
public class SagaiMessage implements Comparable<SagaiMessage> {

    
    private static final Logger logger = LoggerFactory.getLogger(SagaiMessage.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique=true)
    private String content;

    
    @Column(unique=false)
    private String subject;


    @JsonManagedReference
    @OneToMany(mappedBy = "message", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SagaiComment> comments = new ArrayList<>();

    // Additional constructor if you want to set content only
    public SagaiMessage(String content) {
        this.content = content;
        this.comments = new ArrayList<>(); // Ensure comments is initialized
    }

    /** Custom getter to return number of comments 
    */
    public int getNumberOfComment() {
        if (this.comments != null) {
            return comments.size();
        }
        return 0;
    }

    @Override
    public int compareTo(SagaiMessage other){
        String prompt = createPrompt(this.content, other.content);
        return callGroqAPI(prompt);
       //return this.content.compareTo(other.content);
    }
    
    private String createPrompt(String question1, String question2) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Compare if these two questions are similar. Question 1:").append(question1).append("?.");
        prompt.append("Question 2:").append(question2).append("?.");
        prompt.append(" Answer in one word as TRUE or FALSE.");
        return prompt.toString();
    }
    
      private int callGroqAPI(String prompt) {
        logger.warn("prompt= {}", prompt);
        String GROQ_API_KEY = "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"; 
        String API_URL = "https://api.groq.com/openai/v1/chat/completions"; 
        RestTemplate restTemplate = new RestTemplate();
        
        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + GROQ_API_KEY);
        
        // Prepare request body
        String requestBody = String.format("{\"model\": \"llama3-8b-8192\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}", prompt);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        
        // Call the API
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                String result =  jsonNode.get("choices").get(0).get("message").get("content").asText();
                logger.warn("result is {}", result);
                if("TRUE".equals(result)){
                    logger.info("result is true");
                    return 1;
                }
            } else {
                logger.error("Error calling Groq API: {}", response.getStatusCode());
                logger.info("result is false");
                return 0;
            }
        } catch (Exception e) {
            logger.error("Exception while calling Groq API: {}", e.getMessage());
            logger.info("result is false");
            return 0;
        }
        logger.info("result is false");
        return 0;
    }


     /** 2nd telescoping method to create a Message object with parameterized comments
     * @param comments 
     */
    public static SagaiMessage createMessage(String content, String reply) {
        SagaiMessage message = new SagaiMessage();
        message.setContent(content);
    
        List<SagaiComment> comments = new ArrayList<>();
        SagaiComment comment = new SagaiComment();
        comment.setContent(reply);
        comments.add(comment);
        message.setComments(comments);
    
        return message;
    }

    
       
    /** Static method to initialize an array list of Message objects 
     * @return Message[], an array of Message objects
     */
    public static SagaiMessage[] init() {
        ArrayList<SagaiMessage> messages = new ArrayList<>();
        messages.add(createMessage("whats your favorite color", "red"));
        messages.add(createMessage("What is an object in java", "String"));
        return messages.toArray(new SagaiMessage[0]);
    }

      /** Static method to print Message objects from an array
     * @param args, not used
     */
    public static void main(String[] args) {
        // obtain Message from initializer
        SagaiMessage messages[] = init();

        // iterate using "enhanced for loop"
        for( SagaiMessage message : messages) {
            System.out.println(message);  // print object
        }
    }
}
