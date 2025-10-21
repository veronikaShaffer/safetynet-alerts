package com.example.safetynetalerts.controller;

import com.example.safetynetalerts.api.*;
import com.example.safetynetalerts.service.AlertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
public class AlertControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AlertService alertService;

    @Test
    void childAlert_returnOKAndPayload() throws Exception {
        ChildAlertResponse payload = new ChildAlertResponse(
                List.of(new ChildDto("Tenlay", "Boyd", 8)),
                List.of(new PersonNameDto("John", "Boyd"), new PersonNameDto("Ann", "Boyd"))
        );
        when(alertService.childAlertByAddress("1509 Culver St")).thenReturn(payload);
        mvc.perform(get("/childAlert").param("address", "1509 Culver St").accept(String.valueOf(MediaType.APPLICATION_JSON))).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.children[0].firstName").value("Tenlay")).
                andExpect(jsonPath("$.children[0].lastName").value("Boyd")).
                andExpect(jsonPath("$.children[0].age").value("8")).
                andExpect(jsonPath("$.familyMembers[0].firstName").value("John")).
                andExpect(jsonPath("$.familyMembers[1].firstName").value("Ann")).
                andExpect(jsonPath("$.familyMembers[0].lastName").value("Boyd"));
    }

    @Test
    void childAlert_noChildren_returnsMessage() throws Exception {
        ChildAlertResponse mockResponse = new ChildAlertResponse(List.of(), List.of());

        when(alertService.childAlertByAddress("123 Main")).thenReturn(mockResponse);

        mvc.perform(get("/childAlert").param("address", "123 Main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("No children are found at the provided address"));
    }
    @Test
    void childAlert_invalidAddress_returnsBadRequest() throws Exception {
        when(alertService.childAlertByAddress("unknown"))
                .thenThrow(new IllegalArgumentException("Address not found"));

        mvc.perform(get("/childAlert").param("address", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Address is not found in database"));
    }

    @Test
    void childAlert_missingParam_Status400() throws Exception {
        mvc.perform(get("/childAlert")).andExpect(status().isBadRequest());
    }

    @Test
    void phoneAlert_returnOKAndPayload() throws Exception {
        when(alertService.phoneByStation(1)).thenReturn(List.of("841-874-6512", "841-874-8547"));

        mvc.perform(get("/phoneAlert").param("stationNumber", "1").
                        accept(String.valueOf(MediaType.APPLICATION_JSON))).andExpect(status().isOk()).
                andExpect(content().json("[\"841-874-6512\",\"841-874-8547\"]")).
                andExpect(jsonPath("$[0]").value("841-874-6512")).
                andExpect(jsonPath("$[1]").value("841-874-8547"));
    }

    @Test
    void phoneAlert_missingParam_Status400() throws Exception {
        mvc.perform(get("/phoneAlert")).andExpect(status().isBadRequest());
    }

    @Test
    void firestation_returnOkAndPayload() throws Exception {
        FireStationResponse payload = new FireStationResponse(List.of(
                new FireStationPersonDto("John", "X", "22123 Main St", "911-911-9111"),
                new FireStationPersonDto("Nancy", "X", "22123 Main St", "911-911-9111")
        ), 1,1);
        when(alertService.fireStationByNumber(1)).thenReturn(payload);

        mvc.perform(get("/firestation").param("stationNumber", "1").accept(String.valueOf(MediaType.APPLICATION_JSON))).
                andExpect(status().isOk()).
                andExpect(content().contentTypeCompatibleWith(String.valueOf(MediaType.APPLICATION_JSON))).
                andExpect(jsonPath("$.residents[0].firstName").value("John")).
                andExpect(jsonPath("$.residents[0].lastName").value("X")).
                andExpect(jsonPath("$.residents[0].address").value("22123 Main St")).
                andExpect(jsonPath("$.residents[1].firstName").value("Nancy")).
                andExpect(jsonPath("$.residents[1].lastName").value("X")).
                andExpect(jsonPath("$.residents[1].address").value("22123 Main St")).
                andExpect(jsonPath("$.childrenCount").value(1)).
                andExpect(jsonPath("$.adultsCount").value(1));
    }

    @Test
    void firestation_missingParam_Status400() throws Exception {
        mvc.perform(get("/firestation")).andExpect(status().isBadRequest());
    }

}
