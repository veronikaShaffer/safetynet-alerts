package com.example.safetynetalerts.service;

import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonServiceTest {
    private DataRepository dataRepository;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        dataRepository = mock(DataRepository.class);
        personService = new PersonService(dataRepository);
    }

    @Test
    void updatePersonFields_updatesWhenDifferentAfterTrim() {
        // given
        Person john = new Person("John", "Boyd", "1509 Culver St", "Culver", "97451", "841-874-6512", "old@mail.com");
        List<Person> people = List.of(john);
        when(dataRepository.getPersons()).thenReturn(people);
    }

}
