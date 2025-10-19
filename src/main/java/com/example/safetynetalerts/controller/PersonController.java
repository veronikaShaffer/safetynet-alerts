package com.example.safetynetalerts.controller;

import com.example.safetynetalerts.api.PersonCreateRequest;
import com.example.safetynetalerts.api.PersonResponse;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
import com.example.safetynetalerts.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService service;
    private final DataRepository dataRepository;

    public PersonController(PersonService service, DataRepository dataRepository) {
        this.service = service;
        this.dataRepository = dataRepository;
    }

//    @Operation(summary = "Add a new person",
//            responses = { @ApiResponse(responseCode = "200", description = "Success"),
//                    @ApiResponse(responseCode = "400", description = "Bad Request"),
//                    @ApiResponse(responseCode = "500", description = "Server error")})
//    @PostMapping
//    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonCreateRequest body) {
//        Person newPerson = new Person(
//                body.getFirstName(),
//                body.getLastName(),
//                body.getAddress(),
//                body.getCity(),
//                body.getZip(),
//                body.getPhone(),
//                body.getEmail()
//        );
//        dataRepository.getPersons().add(newPerson);
//
//        log.info("New person created: {}", newPerson);
//        //PersonResponse saved = service.create(body);
//        return ResponseEntity.ok(new PersonResponse(newPerson));
//    }


    @Operation(summary = "",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> get(@PathVariable long id) {
        var found = service.get(id);
        return found != null ? ResponseEntity.ok(found) : ResponseEntity.notFound().build();
    }

}

