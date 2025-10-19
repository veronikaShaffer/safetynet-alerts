package com.example.safetynetalerts.model;

import lombok.Data;

import java.util.List;

@Data
public class DataRoot
{
    private List<Person> persons;
    private List<MedicalRecord> medicalrecords;
    private List<Firestation> firestations;
}
