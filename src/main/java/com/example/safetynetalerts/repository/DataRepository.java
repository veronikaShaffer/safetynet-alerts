package com.example.safetynetalerts.repository;

import com.example.safetynetalerts.model.DataRoot;
import com.example.safetynetalerts.model.Firestation;
import com.example.safetynetalerts.model.MedicalRecord;
import com.example.safetynetalerts.model.Person;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Repository
@Getter
public class DataRepository {
    private final ResourceLoader resourceLoader;
    private final ObjectMapper mapper;


    private List<Person> persons = Collections.emptyList();
    private List<MedicalRecord> medicalrecords = Collections.emptyList();
    private List<Firestation> firestations = Collections.emptyList();

    public DataRepository(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @PostConstruct
    void load() {
        try {
            Resource resource = resourceLoader.getResource("classpath:data.json");
            try (InputStream is = resource.getInputStream()) {
                DataRoot root = mapper.readValue(is, DataRoot.class);
                this.persons = root.getPersons();
                this.firestations = root.getFirestations();
                this.medicalrecords = root.getMedicalrecords();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading data json file", e);
        }
    }
    public List<Person> getPersonsByAddress(String address) {
        return persons.stream().filter(p->p.getAddress().equalsIgnoreCase(address)).toList();
    }

    public List<Person> getPersonByStation(Integer stationNumber) {
        List<String> addresses = firestations.stream().
                filter(f -> f.getStation() == stationNumber).
                map(Firestation::getAddress).toList();
        return persons.stream().filter(p -> addresses.contains(p.getAddress())).toList();
    }

    public Optional<MedicalRecord> getMedicalRecord(String firstName, String lastName) {
        return medicalrecords.stream().filter(mr-> mr.getFirstName().
                equalsIgnoreCase(firstName) &&mr.getLastName().equalsIgnoreCase(lastName)).findFirst();
    }

}
