package com.example.safetynetalerts.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChildDto {
    private String firstName;
    private String lastName;
    private Integer age;
}
