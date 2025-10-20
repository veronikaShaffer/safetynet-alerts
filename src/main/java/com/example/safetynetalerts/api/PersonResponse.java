package com.example.safetynetalerts.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonResponse {
    private String firstName;
    private String lastName;
    private String email;
    private Integer age;
}
