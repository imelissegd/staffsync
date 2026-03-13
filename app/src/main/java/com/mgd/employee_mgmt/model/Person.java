package com.mgd.employee_mgmt.model;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.time.Period;

@MappedSuperclass 
public abstract class Person {
    
    private String name;
    private LocalDate dateOfBirth;

    // Constructors
    public Person() {}

    public Person(String name, LocalDate dateOfBirth) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // Common method - calculate age
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Abstraction - abstract method to be implemented by subclasses
    public abstract String getDetails();
    
}