package com.example.safetynetalerts.service;

import com.example.safetynetalerts.api.PersonCreateRequest;
import com.example.safetynetalerts.api.PersonResponse;
import com.example.safetynetalerts.model.Person;
import com.example.safetynetalerts.repository.DataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
public class PersonService {


    private final DataRepository dataRepository;

    public PersonService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public PersonResponse create(PersonCreateRequest req) {
        List<Person> persons = dataRepository.getPersons();


      if (alreadyExists(req.getFirstName(), req.getLastName())) {
          throw new IllegalStateException("Person with same first and last name already exists");
      }

        Person newPerson = new Person(
                req.getFirstName(),
                req.getLastName(),
                        req.getAddress(),
                        req.getCity(),
                        req.getZip(),
                        req.getPhone(),
                        req.getEmail()
        );
        persons.add(newPerson);
        log.info("Added new person: {}", newPerson.getFirstName() + " " + newPerson.getLastName());

        return new PersonResponse(
                newPerson.getFirstName(),
                newPerson.getLastName(),
                newPerson.getEmail(),
                null /// age is calculated from medical record.
        );
    }
    private  boolean alreadyExists(String firstName, String lastName){
        List<Person> persons = dataRepository.getPersons();
       return persons.stream().anyMatch(p -> p.getFirstName().equals( firstName) &&
                p.getLastName().equals(lastName));
    }

    public boolean deletePerson(String firstName, String lastName) {
        List<Person> persons = dataRepository.getPersons();

        boolean removed = persons.removeIf(p ->
                p.getFirstName().equalsIgnoreCase(firstName)
                        && p.getLastName().equalsIgnoreCase(lastName));

        if (removed) {
            log.info("Deleted person: {} {}", firstName, lastName);
        } else {
            log.warn("Person not found for deletion: {} {}", firstName, lastName);
        }

        return removed;
    }

    public enum UpdateStatus { NOT_FOUND, NO_CHANGE, UPDATED }

    public static final class UpdateOutcome {
        public final UpdateStatus status;
        public final PersonResponse person; // present only when UPDATED

        public UpdateOutcome(UpdateStatus status, PersonResponse person) {
            this.status = status;
            this.person = person;
        }
    }


    public UpdateOutcome updatePersonFields(String firstName, String lastName, PersonCreateRequest req) {
        List<Person> persons = dataRepository.getPersons();

        for (Person p : persons) {
            if (p.getFirstName().equalsIgnoreCase(firstName)
                    && p.getLastName().equalsIgnoreCase(lastName)) {

                boolean changed = false;

                if (!differentTrim(p.getAddress(), req.getAddress())) { p.setAddress(req.getAddress()); changed = true; }
                if (!differentTrim(p.getCity(), req.getCity())) { p.setCity(req.getCity()); changed = true; }
                if (!differentTrim(p.getZip(), req.getZip())) { p.setZip(req.getZip()); changed = true; }
                if (!differentTrim(p.getPhone(), req.getPhone())) { p.setPhone(req.getPhone()); changed = true; }
                if (!differentTrim(p.getEmail(), req.getEmail())) { p.setEmail(req.getEmail()); changed = true; }

                if (!changed) {
                    return new UpdateOutcome(UpdateStatus.NO_CHANGE, null);
                }

                log.info("Updated person fields: {} {}", firstName, lastName);
                return new UpdateOutcome(UpdateStatus.UPDATED, toResponse(p)); // ✅ uses the correct PersonResponse
            }
        }

        return new UpdateOutcome(UpdateStatus.NOT_FOUND, null);
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }
    private static boolean differentTrim(String a, String b) {
        String ta = a == null ? null : a.trim();
        String tb = b == null ? null : b.trim();

        if (ta == null && tb == null) return false; // not different
        if (ta == null || tb == null) return true;  // one is missing
        return !ta.equals(tb); // different if not equal
    }

    private PersonResponse toResponse(Person p) {
        return new PersonResponse(
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                null // age is not stored in Person, so return null for now
        );
    }

}
