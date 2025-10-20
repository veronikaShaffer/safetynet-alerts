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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService service) {
        this.personService = service;
    }

    @Operation(summary = "Add a new person with unique firstName and lastName",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @PostMapping("/")
    public ResponseEntity<?> create(@Valid @RequestBody PersonCreateRequest body) {
        try {
            PersonResponse created = personService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException dup) {
            Map<String, String> response = Map.of(
                    "status", "CONFLICT",
                    "message", "Person with the same first and last name already exists"
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

    }

    @Operation(summary = "Delete a person with unique firstName and lastName",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @DeleteMapping("/person")
    public ResponseEntity<?> deletePerson(
            @RequestParam String firstName,
            @RequestParam String lastName) {

        boolean removed = personService.deletePerson(firstName, lastName);

        if (removed) {
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            Map<String, String> msg = Map.of(
                    "message", "Person not found with the provided first and last name"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg); // 404 Not Found
        }
    }

    @Operation(summary = "Update an existing person",
            description = "firstName and lastName cannot be change, but other fields can",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @PutMapping("/person")
    public ResponseEntity<?> updatePerson(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @Valid @RequestBody PersonCreateRequest body) {

        // simple inline record for returning messages as JSON
        record Message(String message) {}

        // Check for blank fields (atomic rule: if any are blank -> reject the request)
        List<String> blankFields = new ArrayList<>();

        if (isBlank(body.getAddress())) blankFields.add("address");
        if (isBlank(body.getCity()))    blankFields.add("city");
        if (isBlank(body.getZip()))     blankFields.add("zip");
        if (isBlank(body.getPhone()))   blankFields.add("phone");
        if (isBlank(body.getEmail()))   blankFields.add("email");

        if (!blankFields.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new Message("These fields are blank; nothing was updated: "
                            + String.join(", ", blankFields)));
        }

        //Call the service
        PersonService.UpdateOutcome outcome =
                personService.updatePersonFields(firstName, lastName, body);

        // Build proper REST responses
        return switch (outcome.status) {
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Message("Person not found with the provided first and last name"));

            case NO_CHANGE -> ResponseEntity.ok(
                    new Message("All information is the same — nothing to update"));

            case UPDATED -> ResponseEntity.ok(outcome.person);
        };
    }

    /**
     * Helper method to detect blank values ("", "   ") safely.
     */
    private boolean isBlank(String s) {
        return s != null && s.trim().isEmpty();
    }
}

