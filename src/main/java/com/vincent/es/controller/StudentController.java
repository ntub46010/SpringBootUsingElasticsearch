package com.vincent.es.controller;

import com.vincent.es.entity.Student;
import com.vincent.es.repository.StudentEsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/students", produces = MediaType.APPLICATION_JSON_VALUE)
public class StudentController {

    @Autowired
    private StudentEsRepository studentEsRepository;

    @PostMapping
    public ResponseEntity<Student> create(@RequestBody Student request) {
        var student = studentEsRepository.insert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(student);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Void> create(@RequestBody List<Student> requests) {
        studentEsRepository.insert(requests);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") String id, @RequestBody Student request) {
        request.setId(id);
        studentEsRepository.save(request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        studentEsRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> get(@PathVariable("id") String id) {
        var student = studentEsRepository.findById(id).orElse(null);
        return student == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(student);
    }
}
