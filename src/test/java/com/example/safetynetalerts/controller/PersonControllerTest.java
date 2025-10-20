package com.example.safetynetalerts.controller;

import com.example.safetynetalerts.api.*;
import com.example.safetynetalerts.service.PersonService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
public class PersonControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PersonService personService;

    @Test
    void createPerson_returnCreatedAndPayload() throws Exception {
        PersonCreateRequest payload = new PersonCreateRequest(
                "Nancy", "Boyd", "123 Main", "Katy","77450","911-987-6789","myemail@gmail.com"
        );
        PersonResponse response = new PersonResponse("Nancy", "Boyd","myemail@gmail.com", null);
        when(personService.create(any(PersonCreateRequest.class))).thenReturn(response);



        mvc.perform(post("/person/")              // or "/person" depending on your mapping
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .accept(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
            {
              "firstName":"Nancy",
              "lastName":"Boyd",
              "address":"123 Main",
              "city":"Katy",
              "zip":"77450",
              "phone":"911-987-0789",
              "email":"myemail@gmail.com"
            }
        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Nancy"));
    }@Test
    void createPerson_whenDuplicate_thenConflict409() throws Exception {
        // Arrange: the service throws the same exception your controller catches
        when(personService.create(any(PersonCreateRequest.class)))
                .thenThrow(new IllegalStateException("duplicate"));

        // Act & Assert
        mvc.perform(post("/person/") // or "/person" if you prefer — both map in your controller
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .accept(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                {
                  "firstName": "Nancy",
                  "lastName": "Boyd",
                  "address": "123 Main",
                  "city": "Katy",
                  "zip": "77450",
                  "phone": "911-987-0789",
                  "email": "myemail@gmail.com"
                }
            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Person with the same first and last name already exists"));
    }
}
