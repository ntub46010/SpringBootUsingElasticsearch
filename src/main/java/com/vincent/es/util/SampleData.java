package com.vincent.es.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincent.es.entity.Student;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SampleData {
    public static List<Student> get() throws IOException {
        var file = new File("students.json");
        return new ObjectMapper().readValue(file, new TypeReference<>() {});
    }
}
