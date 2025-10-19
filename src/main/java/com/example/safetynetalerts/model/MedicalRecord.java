package com.example.safetynetalerts.model;

import lombok.Data;

import java.util.List;

@Data
public class MedicalRecord {
    private String firstName;
    private String lastName;
    private String birthdate;
    private List<String>medications;
    private List<String> allergies;
}
