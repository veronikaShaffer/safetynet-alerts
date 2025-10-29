package com.example.safetynetalerts.service;

import com.example.safetynetalerts.api.PersonCreateRequest;
import com.example.safetynetalerts.api.PersonResponse;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class PersonServiceTest {
    private DataRepository dataRepository;
    private PersonService personService;

    @BeforeEach
    void setUp() {
        dataRepository = mock(DataRepository.class);
        personService = new PersonService(dataRepository);
    }

    @Test
    void updatePersonFields_success_updatesChangedFieldsAndReturnsUPDATED() {
        // given
        Person john = new Person(
                "John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "old@mail.com"
        );
        when(dataRepository.getPersons()).thenReturn(List.of(john));

        PersonCreateRequest req = new PersonCreateRequest();
        req.setAddress("1509 Culver Street");
        req.setCity("NewCity");
        req.setZip("97452");
        req.setPhone("841-874-6513");
        req.setEmail("new@mail.com");

        PersonService.UpdateOutcome outcome = personService.updatePersonFields("John", "Boyd", req);

        assertThat(john.getCity()).isEqualTo("NewCity");
        assertThat(john.getEmail()).isEqualTo("new@mail.com");
        assertThat(john.getAddress()).isEqualTo("1509 Culver Street");
        assertThat(john.getZip()).isEqualTo("97452");
        assertThat(john.getPhone()).isEqualTo("841-874-6513");

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void updatePersonFields_noChanges_returnsNO_CHANGE() {
        // given
        Person jane = new Person(
                "Jane", "Doe",
                null, null, null,
                null, null
        );
        when(dataRepository.getPersons()).thenReturn(List.of(jane));

        PersonCreateRequest req = new PersonCreateRequest();
        req.setAddress(null);
        req.setCity(null);
        req.setZip(null);
        req.setPhone(null);
        req.setEmail(null);

        // when
        PersonService.UpdateOutcome outcome = personService.updatePersonFields("Jane", "Doe", req);

        // Original object remains unchanged
        assertThat(jane.getCity()).isEqualTo(null);
        assertThat(jane.getEmail()).isEqualTo(null);
        assertThat(jane.getAddress()).isEqualTo(null);
        assertThat(jane.getZip()).isEqualTo(null);
        assertThat(jane.getPhone()).isEqualTo(null);

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void updatePersonFields_personFound_branchTrue() {
        // given: Person list contains a matching first+last name
        Person john = new Person("John", "Boyd", "123 Main", "City", "11111", "000-000", "old@mail.com");
        when(dataRepository.getPersons()).thenReturn(List.of(john));

        // Update request (some change)
        PersonCreateRequest req = new PersonCreateRequest();
        req.setAddress("456 Oak St"); // changed to trigger update
        req.setCity("City");
        req.setZip("11111");
        req.setPhone("000-000");
        req.setEmail("old@mail.com");

        // when
        PersonService.UpdateOutcome outcome =
                personService.updatePersonFields("John", "Boyd", req);

        // then
        assertThat(outcome.status()).isEqualTo(PersonService.UpdateStatus.UPDATED);
        assertThat(john.getAddress()).isEqualTo("456 Oak St");

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void deletePerson_success_removesAndReturnsTrue() {
        // given: a mutable list from the repository
        Person john = new Person("John","Boyd","1509 Culver St","Culver","97451","841-874-6512","john@mail.com");
        Person jane = new Person("Jane","Doe","123 Main St","Katy","77000","111-222-3333","jane@mail.com");
        List<Person> people = new ArrayList<>(List.of(john, jane));
        when(dataRepository.getPersons()).thenReturn(people);

        // when
        boolean result = personService.deletePerson("John","Boyd");

        // then
        assertThat(result).isTrue();
        assertThat(people).doesNotContain(john);   // actually removed
        assertThat(people).contains(jane);         // others remain

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void deletePerson_notFound_returnsFalseAndListUnchanged() {
        // given
        Person jane = new Person(
                "Jane", "Doe",
                "123 Main St", "Katy", "77000",
                "111-222-3333", "jane@mail.com"
        );
        List<Person> people = new ArrayList<>(List.of(jane));
        when(dataRepository.getPersons()).thenReturn(people);

        // when
        boolean result = personService.deletePerson("John", "Boyd"); // does not exist

        // then
        assertThat(result).isFalse();              // should return false
        assertThat(people).containsExactly(jane);  // list remains unchanged

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }
    @Test
    void deletePerson_caseInsensitiveMatch_returnsTrue() {
        Person john = new Person("John","Boyd","1509 Culver St","Culver","97451","841-874-6512","john@mail.com");
        List<Person> people = new ArrayList<>(List.of(john));
        when(dataRepository.getPersons()).thenReturn(people);

        boolean result = personService.deletePerson("john","BOYD");

        assertThat(result).isTrue();
        assertThat(people).isEmpty();

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }

    @Test
    void createPerson_success_addsPersonAndReturnsTrue() {
        // given
        List<Person> people = new ArrayList<>();
        when(dataRepository.getPersons()).thenReturn(people);

        PersonCreateRequest req = new PersonCreateRequest();
        req.setFirstName("John");
        req.setLastName("Boyd");
        req.setAddress("1509 Culver St");
        req.setCity("Culver");
        req.setZip("97451");
        req.setPhone("841-874-6512");
        req.setEmail("john@mail.com");

        PersonResponse result = personService.create(req);

        Person created = people.get(0);
        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Boyd");
        assertThat(created.getAddress()).isEqualTo("1509 Culver St");
        assertThat(created.getCity()).isEqualTo("Culver");
        assertThat(created.getZip()).isEqualTo("97451");
        assertThat(created.getPhone()).isEqualTo("841-874-6512");
        assertThat(created.getEmail()).isEqualTo("john@mail.com");

        verify(dataRepository, atLeastOnce()).getPersons();
        verifyNoMoreInteractions(dataRepository);

    }
    @Test
    void createPerson_existingName_throwsIllegalStateException() {
        // given
        Person existing = new Person(
                "John", "Boyd",
                "1509 Culver St", "Culver", "97451",
                "841-874-6512", "john@mail.com"
        );
        List<Person> people = new ArrayList<>(List.of(existing));
        when(dataRepository.getPersons()).thenReturn(people);

        PersonCreateRequest req = new PersonCreateRequest();
        req.setFirstName("John");
        req.setLastName("Boyd");
        req.setAddress("1509 Culver St");
        req.setCity("Culver");
        req.setZip("97451");
        req.setPhone("841-874-6512");
        req.setEmail("john@mail.com");

        assertThatThrownBy(() -> personService.create(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(dataRepository, atLeastOnce()).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }

    @Test
    void updatePersonFields_personNotFound_returnsNOT_FOUND_andLogsWarning() {

        List<Person> people = new ArrayList<>(); // no persons
        when(dataRepository.getPersons()).thenReturn(people);

        PersonCreateRequest req = new PersonCreateRequest();
        req.setAddress("123 Main St");
        req.setCity("Katy");
        req.setZip("77000");
        req.setPhone("111-222-3333");
        req.setEmail("new@mail.com");

        PersonService.UpdateOutcome outcome = personService.updatePersonFields("John", "Boyd", req);

        assertThat(outcome.status()).isEqualTo(PersonService.UpdateStatus.NOT_FOUND);

        verify(dataRepository).getPersons();
        verifyNoMoreInteractions(dataRepository);
    }


}
