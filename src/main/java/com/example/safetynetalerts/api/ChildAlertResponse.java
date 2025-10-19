package com.example.safetynetalerts.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChildAlertResponse {
    private List<ChildDto> children;
    private List<PersonNameDto> familyMembers;
}
