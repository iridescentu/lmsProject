package project.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import project.lms.dto.ResponseDto;
import project.lms.model.Subject;
import project.lms.service.SubjectService;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT })
public class SubjectController {

    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping("/subject/save")
    public ResponseEntity<ResponseDto<Subject>> saveSubject(@RequestBody Subject subject) {
        ResponseDto<Subject> response = subjectService.saveSubject(subject);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ResponseDto<Subject>> getSubjectById(@PathVariable Long subjectId) {
        ResponseDto<Subject> responseDto = subjectService.getSubjectById(subjectId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/subject")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectService.getAllSubjects().getData();
        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @PutMapping("/subject/update/{subjectId}")
    public ResponseEntity<ResponseDto<Subject>> updateSubject(
            @PathVariable Long subjectId,
            @RequestBody Subject updatedSubject) {
        ResponseDto<Subject> responseDto = subjectService.updateSubject(subjectId, updatedSubject);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @DeleteMapping("/subject/delete/{subjectId}")
    public ResponseEntity<ResponseDto<String>> deleteSubject(@PathVariable Long subjectId) {
        ResponseDto<String> responseDto = subjectService.deleteSubject(subjectId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
