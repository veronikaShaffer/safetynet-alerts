package com.example.safetynetalerts.service;

import com.example.safetynetalerts.api.ChildAlertResponse;
import com.example.safetynetalerts.api.ChildDto;
import com.example.safetynetalerts.api.FireStationResponse;
import com.example.safetynetalerts.api.PersonNameDto;
import com.example.safetynetalerts.model.MedicalRecord;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertServiceTest {
    private DataRepository dataRepository;
    private AlertService alertService;

    @BeforeEach
    void setUp() {
        dataRepository = mock(DataRepository.class);
        alertService = new AlertService(dataRepository);
    }
    @Test
    void phoneByStation_distinctAndSorted (){
        // Given persons served by station 1 with duplicate/blank phones
        Person p1 = person("John", "Boyd", "1509 Culver St", "841-874-6512");
        Person p2 = person("Felicia", "Boyd", "1509 Culver St", "841-874-8547");
        Person p3 = person("Alice", "Zed", "29 Elm", "841-874-6512"); // duplicate
        Person p4 = person("Bob", "NoPhone", "100 Oak", ""); // blank -> filtered

        when(dataRepository.getPersonByStation(1)).thenReturn(List.of(p1,p2,p3,p4));
        List<String>phones = alertService.phoneByStation(1);
        assertThat(phones).hasSize(2);
        assertThat(phones).contains(p1.getPhone());
        assertThat(phones).contains(p2.getPhone());
        verify(dataRepository).getPersonByStation(1);
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void childAlertByAddress_ChildrenAndAdults(){
        String address = " 1509 Culver St";
        Person adult1 = person("John", "Boyd", address, "841-874-6512");
        Person adult2 = person("Felicia", "Boyd", address, "841-874-8547");
        Person kid1 = person("Tenley", "Boyd", address, "111-111-1111");
        Person kid2 = person("Roger", "Boyd", address, "222-222-2222");

        when(dataRepository.getPersonsByAddress(address)).thenReturn(List.of(adult1, adult2, kid1, kid2));

        //Adult
        when(dataRepository.getMedicalRecord("John", "Boyd")).thenReturn(Optional.of(mr("John", "Boyd", "03/06/1984")));
        when(dataRepository.getMedicalRecord("Felicia", "Boyd")).thenReturn(Optional.of(mr("Felicia", "Boyd", "10/02/1986")));

        //Children
        when(dataRepository.getMedicalRecord("Tenley", "Boyd")).thenReturn(Optional.of(mr("Tenley", "Boyd", "03/06/2010")));
        when(dataRepository.getMedicalRecord("Roger", "Boyd")).thenReturn(Optional.of(mr("Roger", "Boyd", "02/11/2024")));

        ChildAlertResponse response = alertService.childAlertByAddress(address);

        assertThat(response.getChildren()).extracting(ChildDto::getFirstName).containsExactlyInAnyOrder("Tenley", "Roger");
        assertThat(response.getFamilyMembers()).extracting(PersonNameDto::getFirstName).containsExactlyInAnyOrder("John", "Felicia");

        verify(dataRepository).getPersonsByAddress(address);
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void childAlertByAddress_unknownMedicalRecordIsTreatedAsAdult() {
        String address = "Unknown MR St";
        Person someone = person("Alex", "Doe", address, "999-999-9999");

        when(dataRepository.getPersonsByAddress(address)).thenReturn(List.of(someone));
        when(dataRepository.getMedicalRecord("Alex", "Doe")).thenReturn(Optional.empty());

        ChildAlertResponse res = alertService.childAlertByAddress(address);

        assertThat(res.getChildren()).isEmpty();


        assertThat(res.getFamilyMembers())
                .extracting(PersonNameDto::getFirstName)
                .containsExactly("Alex");

        verify(dataRepository).getPersonsByAddress(address);
        verify(dataRepository).getMedicalRecord("Alex", "Doe");
        verifyNoMoreInteractions(dataRepository);
    }

    // ---------- /firestation ----------
    @Test
    void firestationByNumber_mapsResidentsAndCountsChildrenAndAdults() {
        int station = 1;
        Person adult = person("John", "Boyd", "1509 Culver St", "841-874-6512");
        Person child = person("Roger", "Boyd", "1509 Culver St", "222-222-2222");
        Person unknown = person("NoMR", "Person", "1509 Culver St", "333-333-3333"); // no MR => counted adult

        when(dataRepository.getPersonByStation(station)).thenReturn(List.of(adult, child, unknown));

        when(dataRepository.getMedicalRecord("John", "Boyd"))
                .thenReturn(Optional.of(mr("John", "Boyd", "03/06/1984"))); // adult
        when(dataRepository.getMedicalRecord("Roger", "Boyd"))
                .thenReturn(Optional.of(mr("Roger", "Boyd", "01/09/2015"))); // child (≤ 18)
        when(dataRepository.getMedicalRecord("NoMR", "Person")).thenReturn(Optional.empty()); // unknown → adult

        FireStationResponse res = alertService.fireStationByNumber(station);

        // 1 child (Roger), 2 adults (John + NoMR)
        assertThat(res.getChildrenCount()).isEqualTo(1);
        assertThat(res.getAdultsCount()).isEqualTo(2);

        // Ensure residents are mapped
        assertThat(res.getResidents())
                .extracting(r -> r.getFirstName() + " " + r.getLastName())
                .containsExactlyInAnyOrder("John Boyd", "Roger Boyd", "NoMR Person");

        verify(dataRepository).getPersonByStation(station);
        verify(dataRepository).getMedicalRecord("John", "Boyd");
        verify(dataRepository).getMedicalRecord("Roger", "Boyd");
        verify(dataRepository).getMedicalRecord("NoMR", "Person");
        verifyNoMoreInteractions(dataRepository);
    }


    private static Person person(String first, String last, String address, String phone) {
        Person p = new Person();
        p.setFirstName(first);
        p.setLastName(last);
        p.setAddress(address);
        p.setPhone(phone);
        // city/zip/email not needed for these tests
        return p;
    }

    private static MedicalRecord mr(String first, String last, String birthdate) {
        MedicalRecord mr = new MedicalRecord();
        mr.setFirstName(first);
        mr.setLastName(last);
        mr.setBirthdate(birthdate);
        mr.setMedications(List.of());
        mr.setAllergies(List.of());
        return mr;
    }
}
