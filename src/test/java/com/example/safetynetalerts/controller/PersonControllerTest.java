package com.example.safetynetalerts.controller;

import com.example.safetynetalerts.api.*;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
public class PersonControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PersonService personService;
    @MockitoBean
    private DataRepository dataRepository;

    @Test
    void createPerson_returnCreatedAndPayload() throws Exception {

        PersonResponse response = new PersonResponse("Nancy", "Boyd","myemail@gmail.com", null);
        when(personService.create(any(PersonCreateRequest.class))).thenReturn(response);



        mvc.perform(post("/person")              // or "/person" depending on your mapping
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
        mvc.perform(post("/person") // or "/person" if you prefer — both map in your controller
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
    @Test
    void deletePerson_isNoContent() throws Exception {
        when(personService.deletePerson("Nancy", "Boyd")).thenReturn(true);

        mvc.perform(delete("/person")
                        .param("firstName", "Nancy")
                        .param("lastName", "Boyd"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePerson_NotFound() throws Exception {

        when(dataRepository.getPersons()).thenReturn(List.of());
        mvc.perform(delete("/person").param("firstName", "Janee").param("lastName", "Boyd")).
                andExpect(status().isNotFound());
    }
    @Test
    void updatePerson_success() throws Exception {
        when(personService.updatePersonFields(
                eq("Nancy"), eq("Boyd"), any(PersonCreateRequest.class)))
                .thenReturn(new PersonService.UpdateOutcome(
                        PersonService.UpdateStatus.UPDATED,
                        new PersonResponse("Nancy", "Boyd", "updated@gmail.com", null)
                ));

        mvc.perform(put("/person")
                        .param("firstName", "Nancy")
                        .param("lastName", "Boyd")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                {
                  "firstName": "Nancy",
                  "lastName": "Boyd",
                  "address": "123 Main Street",
                  "city": "Katy",
                  "zip": "77450",
                  "phone": "911-987-0789",
                  "email": "updated@gmail.com"
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Nancy"))
                .andExpect(jsonPath("$.email").value("updated@gmail.com"));
    }
    @Test
    void updatePerson_noChange() throws Exception {
        when(personService.updatePersonFields(
                eq("Nancy"), eq("Boyd"), any(PersonCreateRequest.class)))
                .thenReturn(new PersonService.UpdateOutcome(
                        PersonService.UpdateStatus.NO_CHANGE,
                        new PersonResponse("Nancy", "Boyd", "myemail@gmail.com", null)
                ));

        mvc.perform(put("/person")
                        .param("firstName", "Nancy")
                        .param("lastName", "Boyd")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
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
                .andExpect(status().isNotModified());
    }
    @Test
    void updatePerson_notFound() throws Exception {
        when(personService.updatePersonFields(
                eq("Nancy"), eq("Boyd"), any(PersonCreateRequest.class)))
                .thenReturn(new PersonService.UpdateOutcome(
                        PersonService.UpdateStatus. NOT_FOUND,
                        new PersonResponse("Nancy", "Boyd", "myemail@gmail.com", null)
                ));

        mvc.perform(put("/person")
                        .param("firstName", "Nancy")
                        .param("lastName", "Boyd")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
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
                .andExpect(status().isNotFound());
    }
    private PersonCreateRequest req(
            String address, String city, String zip, String phone, String email
    ) {
        PersonCreateRequest r = new PersonCreateRequest();
        r.setAddress(address);
        r.setCity(city);
        r.setZip(zip);
        r.setPhone(phone);
        r.setEmail(email);
        return r;
    }

}
