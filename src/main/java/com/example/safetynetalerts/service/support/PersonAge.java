package com.example.safetynetalerts.service.support;

import com.example.safetynetalerts.model.Person;

public class PersonAge {
    private final Person person;
    private final Integer age;
    public PersonAge(Person person, Integer age) {
        this.person = person;
        this.age = age;
    }
    public Person getPerson(){
        return person;
    }
    public Integer getAge(){
        return age;
    }
    public boolean isChild(){
        return age!=null && age<= 18;
    }
    public boolean isAdult() {
        return age==null || age>= 18;
    }
}
