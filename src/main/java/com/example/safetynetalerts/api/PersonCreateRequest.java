package com.example.safetynetalerts.api;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PersonCreateRequest {

    @NotBlank(message = "firstName is required")
    @Size(max = 50, message = "firstName must be ≤ 50 chars")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(max = 50, message = "lastName must be ≤ 50 chars")
    private String lastName;

    @NotBlank(message = "address should not be blank")
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String zip;
    @NotBlank
    private String phone;

    @NotBlank
    @Email(message = "email must be a valid email")
    @Size(max = 120, message = "email must be ≤ 120 chars")
    private String email;

}
