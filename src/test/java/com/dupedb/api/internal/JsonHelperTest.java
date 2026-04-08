package com.dupedb.api.internal;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonHelperTest {

    // Test record with camelCase fields
    record Person(String firstName, String lastName, int age) {}

    @Test
    void fromJsonDeserializesSnakeCaseToCamelCase() {
        String json = "{\"first_name\":\"Bob\",\"last_name\":\"Smith\",\"age\":30}";
        Person person = JsonHelper.fromJson(json, Person.class);

        assertEquals("Bob", person.firstName());
        assertEquals("Smith", person.lastName());
        assertEquals(30, person.age());
    }

    @Test
    void toJsonSerializesCamelCaseToSnakeCase() {
        Person person = new Person("Alice", "Jones", 25);
        String json = JsonHelper.toJson(person);

        assertTrue(json.contains("\"first_name\""));
        assertTrue(json.contains("\"Alice\""));
        assertTrue(json.contains("\"last_name\""));
        assertTrue(json.contains("\"Jones\""));
        assertTrue(json.contains("\"age\""));
        assertFalse(json.contains("\"firstName\""));
        assertFalse(json.contains("\"lastName\""));
    }

    @Test
    void fromJsonWithGenericTypeToken() {
        String json = "[{\"first_name\":\"Bob\",\"age\":30},{\"first_name\":\"Alice\",\"age\":25}]";
        Type listType = new TypeToken<List<Person>>() {}.getType();
        List<Person> people = JsonHelper.fromJson(json, listType);

        assertEquals(2, people.size());
        assertEquals("Bob", people.get(0).firstName());
        assertEquals("Alice", people.get(1).firstName());
    }

    @Test
    void fromJsonHandlesNullFields() {
        String json = "{\"first_name\":null,\"age\":0}";
        Person person = JsonHelper.fromJson(json, Person.class);

        assertNull(person.firstName());
        assertEquals(0, person.age());
    }

    @Test
    void roundTripPreservesData() {
        Person original = new Person("Charlie", "Brown", 40);
        String json = JsonHelper.toJson(original);
        Person deserialized = JsonHelper.fromJson(json, Person.class);

        assertEquals(original.firstName(), deserialized.firstName());
        assertEquals(original.lastName(), deserialized.lastName());
        assertEquals(original.age(), deserialized.age());
    }
}
