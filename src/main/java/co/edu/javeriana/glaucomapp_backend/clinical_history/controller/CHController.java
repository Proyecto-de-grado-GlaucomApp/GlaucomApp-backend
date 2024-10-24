package co.edu.javeriana.glaucomapp_backend.clinical_history.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamRes;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.exam.ExamsResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientRequest;
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.pacient.PacientResponse;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.ExamService;
import co.edu.javeriana.glaucomapp_backend.clinical_history.service.PacientService;
import co.edu.javeriana.glaucomapp_backend.common.JwtUtil;
import co.edu.javeriana.glaucomapp_backend.common.exceptions.UnauthorizedException;

@RequestMapping("/mobile/clinical_history")
@RestController
public class CHController {

    private final PacientService pacientService;

    private final ExamService examService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    public CHController(PacientService pacientService, ExamService examService) {
        this.pacientService = pacientService;
        this.examService = examService;
    }

    @PostMapping("/save/pacient")
    public ResponseEntity<String> savePatience(@RequestHeader("Authorization") String token,
            @RequestBody PacientRequest pacient) {
        String ophtalIdString = validateToken(token);
        try {
            pacientService.savePacient(pacient, ophtalIdString);
            return ResponseEntity.ok("Pacient saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Register failed: " + e.getMessage());
        }
    }

    @GetMapping("/get/pacients")
    public ResponseEntity<List<PacientResponse>> getPacientsByOphtal(@RequestHeader("Authorization") String token,  @RequestParam int startIndex, @RequestParam int endIndex) {
        String ophtalIdString = validateToken(token);
        try {
            List<PacientResponse> pacients = pacientService.getPacientsByOphtal(ophtalIdString, startIndex, endIndex);
            return ResponseEntity.ok(pacients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/pacient/{pacientId}")
    public ResponseEntity<String> deletePacient(@RequestHeader("Authorization") String token, @PathVariable String pacientId) {
        String ophtalIdString = validateToken(token);
        try {
            System.out.println("Pacient to delete: " + pacientId);
            pacientService.deletePacient(ophtalIdString, pacientId);
            return ResponseEntity.ok("Pacient deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Pacient cannot be deleted: " + e.getMessage());
        }
    }

    @PostMapping("save/exam")
    public ResponseEntity<String> saveExam(@RequestHeader("Authorization") String token, @RequestBody ExamRequest examRequest){
        String ophtalIdString = validateToken(token);
        try {
            examService.saveExam(ophtalIdString, examRequest);
            return ResponseEntity.ok("Exam saved succesafully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Register failed: ");
        }
        
    }

    @GetMapping("get/exams")
    public ResponseEntity<List<ExamsResponse>> getExamsByPacient(@RequestHeader("Authorization") String token, @RequestParam String pacientId, @RequestParam int startIndex, @RequestParam int endIndex){
        String ophtalIdString = validateToken(token);
        try {
            List<ExamsResponse> exams = examService.getExamsByPacient(ophtalIdString, pacientId, startIndex, endIndex);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("delete/exam/{examId}")
    public ResponseEntity<String> deleteExam (@RequestHeader("Authorization") String token,  @PathVariable String examId, @RequestParam String pacientId){
        String ophtalIdString = validateToken(token);
        try {
            examService.deleteExam(ophtalIdString, pacientId,examId);
            return ResponseEntity.ok("Exam deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Exam cannot be deleted: " + e.getMessage());
        }
    }

    @GetMapping("get/exam")
    public ResponseEntity<ExamRes> getExam(@RequestHeader("Authorization") String token,  @RequestParam String examId, @RequestParam String pacientId){
        String ophtalIdString = validateToken(token);
        try {
            ExamRes exam = examService.getExamById(ophtalIdString,pacientId, examId);
            return ResponseEntity.ok(exam);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String validateToken(String token) {
        String ophtalIdString = jwtUtil.extractIdFromToken(token);
        if (ophtalIdString == null) {
            throw new UnauthorizedException("Invalid Token or ophtalmologist ID not found.");
        }
        return ophtalIdString;
    }


}
