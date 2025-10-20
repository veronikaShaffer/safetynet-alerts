package com.example.safetynetalerts.controller;

import com.example.safetynetalerts.api.ChildAlertResponse;
import com.example.safetynetalerts.api.FireStationResponse;
import com.example.safetynetalerts.api.PersonResponse;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @Operation(summary = "This URL must return a list of people covered by the corresponding fire station.",
            description = "The list must include the following specific information: first name, last name, address," +
                    "phone number. Additionally, it must provide a count of the number of adults and the" +
                    "number of children (any individual aged 18 years or younger) in the served area.",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "500", description = "Server error")})
    @GetMapping("/firestation")
    public ResponseEntity<FireStationResponse>fireStation(@RequestParam("stationNumber") int stationNumber) {
        log.info("Received request : GET firestation ?station number= {}", stationNumber);
        FireStationResponse result = alertService.fireStationByNumber(stationNumber);
        log.info("Returning persons covered by fire station {} -> {}", stationNumber, result);
       return ResponseEntity.ok(result);
    }

    @Operation(summary = "This URL must return a list of phone numbers of residents served by the fire station.",
            description = "It\n" +
                    "will be used to send emergency text messages to specific households.\n",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @GetMapping("/phoneAlert")
    public ResponseEntity<List<String>>phoneAlert(@RequestParam("stationNumber") int stationNumber){
        log.info("Received request : GET phoneAlert for fire station number= {}", stationNumber);
        List<String> phones = alertService.phoneByStation(stationNumber);
        log.info("Returning {} phones numbers for fire station #{} -> {} ",phones.size(), stationNumber, phones);
        return ResponseEntity.ok(phones);
    }

    @Operation(summary = "This URL must return a list of children (any individual aged 18 years or younger) living at this address",
            description = "The list must include the first name and last name of each child, their\n" +
                    "age, and a list of other household members. If no children are found, this URL may\n" +
                    "return an empty string.",
            responses = { @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad Request"),
                    @ApiResponse(responseCode = "500", description = "Server error")})
    @GetMapping("/childAlert")
    public ResponseEntity<?> childAlert(@RequestParam("address") String address) {

        try {
            ChildAlertResponse childAlertResponse = alertService.childAlertByAddress(address);
            log.info("Returning address {}  for the child alert", address);
            if (childAlertResponse.getChildren().isEmpty()) {
                Map<String, String> msg = Map.of(
                        "message", "No children are found at the provided address"
                );
                return ResponseEntity.ok(msg);
            }
            return ResponseEntity.ok(childAlertResponse);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = Map.of(
                    "message", "Address is not found in database"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
