package com.example.safetynetalerts.service;

import com.example.safetynetalerts.api.*;
import com.example.safetynetalerts.model.Firestation;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
import com.example.safetynetalerts.service.support.PersonAge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlertService {
    private final DataRepository dataRepository;

    public AlertService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public List<Person> peopleCoveredByStation(int stationNumber) {
        Set<String> addresses = dataRepository.getFirestations().stream()
                .filter(f->f.getStation() == stationNumber).map(f->f.getAddress()).collect(Collectors.toSet());
      //  log.info("Addresses covered by station {}:{}", stationNumber, addresses);

        return dataRepository.getPersons().stream()
                .filter(p->addresses.contains(p.getAddress())).collect(Collectors.toList());
    }
    public List<String>phoneByStation(int stationNumber) {

        //from persons, select those living at the addresses
        List<String> phones =  dataRepository.getPersonByStation(stationNumber).
                stream().map(Person::getPhone).filter(p->p != null && !p.isBlank()).distinct().
                sorted().collect(Collectors.toList());
        log.info("Phones covered by station# {} are:{}", stationNumber, phones);
        return phones;

    }
    public int computeAge(String birthdate){
        DateTimeFormatter  formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate dateOfBirth = LocalDate.parse(birthdate, formatter);
        return Period.between(dateOfBirth,LocalDate.now()).getYears();
    }
    private PersonAge toPersonAge(Person person) {
        Integer age = dataRepository.getMedicalRecord(person.getFirstName(), person.getLastName()).
                map(mr->computeAge(mr.getBirthdate())).orElse(null);
        return new PersonAge(person, age);
    }



    public ChildAlertResponse childAlertByAddress(String address){

        List<Person> household = dataRepository.getPersonsByAddress(address);
        if(household.isEmpty()){
            log.warn("Address {} is not found in database", address);
            throw new IllegalArgumentException("Address " + address + " is not found in database");
        }
        List<PersonAge> withAge = household.stream().
                map(this::toPersonAge).toList();


        // check who is less  = than 18 yo
        List<ChildDto> children = withAge.stream().filter(
                PersonAge::isChild).map(pwa -> new ChildDto(pwa.getPerson().getFirstName(),
                pwa.getPerson().getLastName(),
                pwa.getAge()
        )).collect(Collectors.toList());
        //Other adults


        List<PersonNameDto> familyMembers =withAge.stream().filter(
                PersonAge::isAdult).map(pwa -> new PersonNameDto(pwa.getPerson().getFirstName(),
                pwa.getPerson().getLastName()
        )).collect(Collectors.toList());
        log.info("childAlert :  {} children at the address {} with {} family members",children,address,familyMembers);
        return new ChildAlertResponse(children, familyMembers);
    }
     public FireStationResponse fireStationByNumber(int stationNumber){
        List<Person> residence = dataRepository.getPersonByStation( stationNumber);
        List<PersonAge> ages = residence.stream().map(this::toPersonAge).toList();
        int childrenCount = (int) ages.stream().filter(PersonAge::isChild).count();
        int adultCount = (int) ages.stream().filter(PersonAge::isAdult).count();

        List<FireStationPersonDto> dtoList = ages.stream().map(
                pwa -> new FireStationPersonDto(
                        pwa.getPerson().getFirstName(),
                        pwa.getPerson().getLastName(),
                        pwa.getPerson().getAddress(),
                        pwa.getPerson().getPhone()
                )).toList();

        return new FireStationResponse(dtoList, childrenCount, adultCount);
    }
}
