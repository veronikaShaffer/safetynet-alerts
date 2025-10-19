package com.example.safetynetalerts.service;

import com.example.safetynetalerts.api.PersonCreateRequest;
import com.example.safetynetalerts.api.PersonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class PersonService {

    private final AtomicLong ids = new AtomicLong(1);
    private final Map<Long, PersonResponse> store = new ConcurrentHashMap<>();

    public PersonResponse create(PersonCreateRequest req) {
        long id = ids.getAndIncrement();
        PersonResponse res = new PersonResponse(
                id,
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getAge()
        );
        store.put(id, res);
        return res;
    }

    public PersonResponse get(long id) {
        return store.get(id);
    }
}
