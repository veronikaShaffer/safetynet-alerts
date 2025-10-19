package com.example.safetynetalerts.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonNameDto {
    private String firstName;
    private String lastName;
}
