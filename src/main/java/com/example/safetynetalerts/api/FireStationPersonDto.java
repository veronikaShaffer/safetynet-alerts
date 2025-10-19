package com.example.safetynetalerts.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FireStationPersonDto {
    private String firstName;
    private String lastName;
    private String address;
    private String phoneNumber;
}
