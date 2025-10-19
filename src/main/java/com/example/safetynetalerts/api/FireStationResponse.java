package com.example.safetynetalerts.api;

import com.example.safetynetalerts.model.Firestation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FireStationResponse {
    private List<FireStationPersonDto> residents;
    private int childrenCount;
    private int adultsCount;
}

