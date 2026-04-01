package com.example.xiaozhimed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataMongoTest(properties = "spring.data.mongodb.database=xiaozhimed_mongo_crud_test")
class MongoCrudTest {

    private static final String COLLECTION_NAME = "patient_records_test";

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(COLLECTION_NAME);
    }

    @Test
    void shouldCreateDocument() {
        PatientRecord record = new PatientRecord("p001", "Alice", 28, "headache");

        mongoTemplate.save(record, COLLECTION_NAME);

        PatientRecord saved = mongoTemplate.findById("p001", PatientRecord.class, COLLECTION_NAME);
        assertNotNull(saved);
        assertEquals("Alice", saved.getName());
        assertEquals(28, saved.getAge());
        assertEquals("headache", saved.getSymptom());
    }

    @Test
    void shouldReadDocumentByQuery() {
        mongoTemplate.save(new PatientRecord("p002", "Bob", 35, "cough"), COLLECTION_NAME);

        Query query = Query.query(Criteria.where("name").is("Bob"));
        PatientRecord found = mongoTemplate.findOne(query, PatientRecord.class, COLLECTION_NAME);

        assertNotNull(found);
        assertEquals("p002", found.getId());
        assertEquals("cough", found.getSymptom());
    }

    @Test
    void shouldUpdateDocument() {
        mongoTemplate.save(new PatientRecord("p003", "Cindy", 41, "fever"), COLLECTION_NAME);

        PatientRecord existing = mongoTemplate.findById("p003", PatientRecord.class, COLLECTION_NAME);
        assertNotNull(existing);
        existing.setSymptom("recovered");
        existing.setAge(42);
        mongoTemplate.save(existing, COLLECTION_NAME);

        PatientRecord updated = mongoTemplate.findById("p003", PatientRecord.class, COLLECTION_NAME);
        assertNotNull(updated);
        assertEquals(42, updated.getAge());
        assertEquals("recovered", updated.getSymptom());
    }

    @Test
    void shouldDeleteDocument() {
        mongoTemplate.save(new PatientRecord("p004", "David", 30, "stomachache"), COLLECTION_NAME);

        Query query = Query.query(Criteria.where("_id").is("p004"));
        mongoTemplate.remove(query, COLLECTION_NAME);

        PatientRecord deleted = mongoTemplate.findById("p004", PatientRecord.class, COLLECTION_NAME);
        assertNull(deleted);
        assertFalse(mongoTemplate.collectionExists(COLLECTION_NAME)
                && mongoTemplate.exists(query, COLLECTION_NAME));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class PatientRecord {

        @Id
        private String id;
        private String name;
        private int age;
        private String symptom;

    }
}
