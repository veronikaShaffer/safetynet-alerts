package com.example.safetynetalerts.controller;

import com.example.safetynetalerts.repository.DataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class DataDebugController {

    private final DataRepository repo;

    public DataDebugController(DataRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "GET counts for persoons, fire station and med records",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @GetMapping("/api/debug/counts")
    public ResponseEntity<Map<String, Object>> counts() {
        return ResponseEntity.ok(Map.of(
                "persons", repo.getPersons().size(),
                "firestations", repo.getFirestations().size(),
                "medicalrecords", repo.getMedicalrecords().size()
        ));
    }
}
