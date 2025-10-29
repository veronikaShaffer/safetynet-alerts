package com.example.safetynetalerts.repository;

import com.example.safetynetalerts.model.Firestation;
import com.example.safetynetalerts.model.MedicalRecord;
import com.example.safetynetalerts.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataRepositoryTest {

    private DataRepository repo;

    private static Person person(String firstName, String lastName, String address, String city, String zip, String phone, String email) {
        Person p = new Person();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setAddress(address);
        p.setCity(city);
        p.setZip(zip);
        p.setPhone(phone);
        p.setEmail(email);
        return p;
    }

    private static Firestation firestation(String address, int station) {
        Firestation f = new Firestation();
        f.setAddress(address);
        f.setStation(station);
        return f;
    }
    private static MedicalRecord  medicalrecord(String firstName, String lastName, String birthdate, List<String> medications, List<String> allergies) {
        MedicalRecord m = new MedicalRecord();
        m.setFirstName(firstName);
        m.setLastName(lastName);
        m.setBirthdate(birthdate);
        m.setMedications(medications);
        m.setAllergies(allergies);
        return m;
    }

    @BeforeEach
    void setUp() {
        // Mock the ResourceLoader (we won’t actually load anything from it)
        ResourceLoader mockLoader = mock(ResourceLoader.class);
        repo = new DataRepository(mockLoader);

        // Create in-memory test data
        List<Person> persons = List.of(
                person("John", "Boyd", "1509 Culver St","Katy","77450", "841-874-6912","katy5@gmail.com"),
                person("Jacob", "Boyd", "1509 Culver St","Katy","77450", "841-874-6712","katy4@gmail.com"), // duplicate phone
                person("Felicia", "Boyd", "1509 Culver St","Katy","77450", "841-874-6512","katy3@gmail.com"),
                person("Tenley", "Boyd", "29 15th St", "Katy","77450", "841-874-6519","katy2@gmail.com"),
                person("Roger", "Boyd", "834 Binoc Ave", "Katy","77450", "841-874-6510","katy1@gmail.com")
        );

        List<Firestation> firestations = List.of(
                firestation("1509 Culver St", 1),
                firestation("834 Binoc Ave", 1),
                firestation("29 15th St", 2)
        );

        List<MedicalRecord> medicalrecords = List.of(
                medicalrecord("John", "Boyd","02/09/1999",List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("xilliathal")),
                medicalrecord("Jacob", "Boyd","12/09/2024",List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("xilliathal")),
                medicalrecord("Felicia", "Boyd","02/09/1998",List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("xilliathal")),
                medicalrecord("Tenley", "Boyd","10/09/2001",List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("xilliathal")),
                medicalrecord("Roger", "Boyd","02/07/2002",List.of("aznol:350mg", "hydrapermazol:100mg"), List.of("xilliathal"))

        );

        // Inject fields directly (bypassing JSON loading)
        ReflectionTestUtils.setField(repo, "persons", persons);
        ReflectionTestUtils.setField(repo, "firestations", firestations);
        ReflectionTestUtils.setField(repo, "medicalrecords", medicalrecords);
    }

    @Test
    void getPersonByAddress_returnsAllAtAddress_caseInsensitive() {
        List<Person> result = repo.getPersonsByAddress("1509 culver st");
        assertThat(result)
                .hasSize(3)
                .extracting(Person::getFirstName)
                .containsExactlyInAnyOrder("John", "Jacob", "Felicia");
    }

    @Test
    void getPersonByAddress_unknownAddress_returnsEmptyList() {
        assertThat(repo.getPersonsByAddress("Unknown Address")).isEmpty();
    }

    @Test
    void getPersonByStation_returnsAllPeopleCoveredByStation() {
        List<Person> station1 = repo.getPersonByStation(1);
        assertThat(station1)
                .hasSize(4)
                .extracting(Person::getAddress)
                .containsExactlyInAnyOrder("1509 Culver St", "1509 Culver St", "1509 Culver St", "834 Binoc Ave");

        List<Person> station2 = repo.getPersonByStation(2);
        assertThat(station2)
                .singleElement()
                .extracting(Person::getAddress)
                .isEqualTo("29 15th St");

        assertThat(repo.getPersonByStation(999)).isEmpty();
    }
    @Test
    void getMedicalRecord_returnsMedicalRecord(){
        Optional<MedicalRecord> medRecord = repo.getMedicalRecord("John", "Boyd");
        assertThat(medRecord)
                .isPresent();  // AssertJ built-in for Optional

        // Now check fields inside it
        MedicalRecord record = medRecord.get();
        assertThat(record.getFirstName()).isEqualTo("John");
        assertThat(record.getLastName()).isEqualTo("Boyd");
        assertThat(record.getBirthdate()).isEqualTo("02/09/1999");
        assertThat(record.getMedications()).containsExactly("aznol:350mg", "hydrapermazol:100mg");
        assertThat(record.getAllergies()).containsExactly("xilliathal");
    }
    @Test
    void getMedicalRecord_firstNameMismatch_isEmpty() {
        // first name differs → short-circuit prevents evaluating last name
        Optional<MedicalRecord> medRecord = repo.getMedicalRecord("Johnny", "Boyd");
        assertThat(medRecord).isEmpty();
    }
    @Test
    void getMedicalRecord_lastNameMismatch_isEmpty() {
        // first name matches, forces evaluation of second condition
        Optional<MedicalRecord> medRecord = repo.getMedicalRecord("John", "Smith");
        assertThat(medRecord).isEmpty();
    }
    @Test
    void load_whenJsonReadFails_shouldThrowRuntimeException() throws IOException {
        // Mock dependencies
        ResourceLoader mockLoader = mock(ResourceLoader.class);
        Resource mockResource = mock(Resource.class);

        // Make the loader return our mock resource
        when(mockLoader.getResource("classpath:data.json")).thenReturn(mockResource);

        // Simulate IOException when trying to open the InputStream
        when(mockResource.getInputStream()).thenThrow(new IOException("Simulated I/O error"));

        // Create repository with the mocked loader
        DataRepository repo = new DataRepository(mockLoader);

        // When load() is called, it should throw RuntimeException
        assertThatThrownBy(repo::load)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error loading data json file")
                .hasCauseInstanceOf(IOException.class);
    }
    @Test
    void load_whenJsonReadSucceeds_setsCollections() throws Exception {
        ResourceLoader mockLoader = mock(ResourceLoader.class);
        Resource mockResource = mock(Resource.class);

        String json = """
    {
      "persons": [
        {"firstName":"John","lastName":"Boyd","address":"1509 Culver St","city":"Katy","zip":"77450","phone":"841-874-6512","email":"john@example.com"}
      ],
      "firestations": [
        {"address":"1509 Culver St","station":1}
      ],
      "medicalrecords": [
        {"firstName":"John","lastName":"Boyd","birthdate":"02/09/1999","medications":["aznol:350mg"],"allergies":["xilliathal"]}
      ]
    }
    """;

        when(mockLoader.getResource("classpath:data.json")).thenReturn(mockResource);
        when(mockResource.getInputStream()).thenReturn(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))
        );

        DataRepository repo = new DataRepository(mockLoader);

        // Act
        repo.load();

        // Assert
        assertThat(ReflectionTestUtils.getField(repo, "persons")).asList().hasSize(1);
        assertThat(ReflectionTestUtils.getField(repo, "firestations")).asList().hasSize(1);
        assertThat(ReflectionTestUtils.getField(repo, "medicalrecords")).asList().hasSize(1);
    }

}
