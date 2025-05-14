package com.nighthawk.spring_portfolio.mvc.trains;

import java.util.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
@Convert(attributeName = "train", converter = JsonType.class)
public class TrainOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="order_id")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="train_id")
    private Train train;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastTime;

    private boolean repeat; //may have different effects depending on SubClass interpretation

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,String> orderInfo; //Key, Value

    public boolean doSimulation(){
        return true;
    }; //do a single/partial simulation step, returns completion status
}
