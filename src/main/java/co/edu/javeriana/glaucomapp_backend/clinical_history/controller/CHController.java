/**
 * CHController is a REST controller that handles HTTP requests related to clinical history operations.
 * It provides endpoints for saving, retrieving, and deleting patients and exams.
 * 
 * Endpoints:
 * 
 *   POST /mobile/clinical_history/save/pacient - Save a new patient
 *   GET /mobile/clinical_history/get/pacients - Retrieve a list of patients
 *   DELETE /mobile/clinical_history/delete/pacient/{pacientId} - Delete a patient by ID
 *   POST /mobile/clinical_history/save/exam - Save a new exam
 *   GET /mobile/clinical_history/get/exams - Retrieve a list of exams for a patient
 *   DELETE /mobile/clinical_history/delete/exam/{examId} - Delete an exam by ID
 *   GET /mobile/clinical_history/get/exam - Retrieve a specific exam by ID
 * 
 * 
 * Each endpoint requires an Authorization header with a valid JWT token.
 * 
 * Services used:
 * 
 *   PacientService - Service for patient-related operations
 *   ExamService - Service for exam-related operations
 * 
 * 
 * Exceptions handled:
 * 
 *   IllegalArgumentException - For invalid input data
 *   AccessDeniedException - For unauthorized access attempts
 *   UnauthorizedException - For invalid or missing tokens
 *   Exception - For any other unexpected errors
 * 
 * 
 * Utility methods:
 * 
 *   validateToken - Validates the JWT token and extracts the ophthalmologist ID
 * 
 * 
 * Dependencies:
 * 
 *   JwtUtil - Utility class for handling JWT tokens
 * 
 * 
 * Annotations:
 * 
 *   @RequestMapping - Maps HTTP requests to handler methods
 *   @RestController - Indicates that this class is a REST controller
 *   @Autowired - Marks a constructor or field for dependency injection
 *   @PostMapping - Maps HTTP POST requests to handler methods
 *   @GetMapping - Maps HTTP GET requests to handler methods
 *   @DeleteMapping - Maps HTTP DELETE requests to handler methods
 * 
 */


package co.edu.javeriana.glaucomapp_backend.clinical_history.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import co.edu.javeriana.glaucomapp_backend.clinical_history.model.ErrorResponse;
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

    /**
     * Endpoint to save a patient's information.
     *
     * @param token the authorization token from the request header
     * @param pacient the patient information to be saved
     * @return a ResponseEntity containing a success message if the patient is saved successfully,
     *         a bad request message if there is an IllegalArgumentException,
     *         or an internal server error message if an unexpected error occurs
     */
    @PostMapping("/save/pacient")
    public ResponseEntity<String> savePatience(@RequestHeader("Authorization") String token,
            @RequestBody PacientRequest pacient) {
        String ophtalIdString = validateToken(token);
        try {
            pacientService.savePacient(pacient, ophtalIdString);
            return ResponseEntity.ok("Pacient saved successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred, try later");
        }
    }

    /**
     * Retrieves a list of patients associated with an ophthalmologist.
     *
     * @param token      the authorization token to validate the request
     * @param startIndex the starting index for pagination
     * @param endIndex   the ending index for pagination
     * @return a ResponseEntity containing a list of PacientResponse objects if successful,
     *         or an appropriate error response if an error occurs
     */
    @GetMapping("/get/pacients")
    public ResponseEntity<List<PacientResponse>> getPacientsByOphtal(@RequestHeader("Authorization") String token,
            @RequestParam int startIndex, @RequestParam int endIndex) {
        String ophtalIdString = validateToken(token);
        try {
            List<PacientResponse> pacients = pacientService.getPacientsByOphtal(ophtalIdString, startIndex, endIndex);
            return ResponseEntity.ok(pacients);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Error retrieving pacients", e.getMessage())
                    .body(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).header("Unexpected error occurred , try later").body(new ArrayList<>());
        }
    }

    /**
     * Deletes a patient record based on the provided patient ID.
     *
     * @param token The authorization token from the request header.
     * @param pacientId The ID of the patient to be deleted.
     * @return A ResponseEntity containing a success message if the deletion is successful,
     *         or an error message if the deletion fails.
     * @throws IllegalArgumentException if the provided patient ID is invalid.
     * @throws AccessDeniedException if the user does not have permission to delete the patient.
     * @throws Exception if an unexpected error occurs during the deletion process.
     */
    @DeleteMapping("/delete/pacient/{pacientId}")
    public ResponseEntity<String> deletePacient(@RequestHeader("Authorization") String token,
            @PathVariable String pacientId) {
        String ophtalIdString = validateToken(token);
        try {
            System.out.println("Pacient to delete: " + pacientId);
            pacientService.deletePacient(ophtalIdString, pacientId);
            return ResponseEntity.ok("Pacient deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Deletion failed: " + e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body("Deletion failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred, try later");
        }
    }

    /**
     * Endpoint to save an exam.
     *
     * @param token       The authorization token from the request header.
     * @param examRequest The request body containing exam details.
     * @return A ResponseEntity containing a success message if the exam is saved successfully,
     *         a bad request message if there is an IllegalArgumentException,
     *         or an internal server error message if an unexpected error occurs.
     */
    @PostMapping("save/exam")
    public ResponseEntity<String> saveExam(@RequestHeader("Authorization") String token,
            @RequestBody ExamRequest examRequest) {
        String ophtalIdString = validateToken(token);
        try {
            examService.saveExam(ophtalIdString, examRequest);
            return ResponseEntity.ok("Exam saved succesafully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred, try later");
        }

    }

    /**
     * Retrieves a list of exams for a specific patient within a specified range.
     *
     * @param token the authorization token from the request header
     * @param pacientId the ID of the patient whose exams are to be retrieved
     * @param startIndex the starting index of the exams to be retrieved
     * @param endIndex the ending index of the exams to be retrieved
     * @return a ResponseEntity containing a list of ExamsResponse objects if successful,
     *         or an appropriate error response if an error occurs
     */
    @GetMapping("get/exams")
    public ResponseEntity<List<ExamsResponse>> getExamsByPacient(@RequestHeader("Authorization") String token,
            @RequestParam String pacientId, @RequestParam int startIndex, @RequestParam int endIndex) {
        String ophtalIdString = validateToken(token);
        try {
            List<ExamsResponse> exams = examService.getExamsByPacient(ophtalIdString, pacientId, startIndex, endIndex);
            return ResponseEntity.ok(exams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Error retrieving exams", e.getMessage()).body(new ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).header("Unexpected error occurred , try later").body(new ArrayList<>());
        }
    }

    /**
     * Deletes an exam based on the provided exam ID, patient ID, and authorization token.
     *
     * @param token the authorization token from the request header
     * @param examId the ID of the exam to be deleted
     * @param pacientId the ID of the patient associated with the exam
     * @return a ResponseEntity containing a success message if the deletion is successful,
     *         or an error message if the deletion fails due to an invalid argument, unauthorized access,
     *         or any other unexpected error
     */
    @DeleteMapping("delete/exam/{examId}")
    public ResponseEntity<String> deleteExam(@RequestHeader("Authorization") String token, @PathVariable String examId,
            @RequestParam String pacientId) {
        String ophtalIdString = validateToken(token);
        try {
            examService.deleteExam(ophtalIdString, pacientId, examId);
            return ResponseEntity.ok("Exam deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Deletion failed: " + e.getMessage());
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body("Deletion failed: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred, try later");
        }
    }

    /**
     * Retrieves an exam based on the provided exam ID and patient ID.
     *
     * @param token The authorization token from the request header.
     * @param examId The ID of the exam to be retrieved.
     * @param pacientId The ID of the patient to whom the exam belongs.
     * @return A ResponseEntity containing the exam details if found, or an appropriate error response.
     * 
     * Possible responses:
     * - 200 OK: The exam was successfully retrieved.
     * - 400 Bad Request: The request was invalid, typically due to an illegal argument.
     * - 401 Unauthorized: The token is invalid or missing.
     * - 403 Forbidden: Access to the requested resource is denied.
     * - 500 Internal Server Error: An unexpected error occurred.
     */
    @GetMapping("get/exam")
    public ResponseEntity<?> getExam(@RequestHeader("Authorization") String token, @RequestParam String examId,
            @RequestParam String pacientId) {
        String ophtalIdString = validateToken(token);
        try {
            ExamRes exam = examService.getExamById(ophtalIdString, pacientId, examId);
            return ResponseEntity.ok(exam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Bad Request", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(new ErrorResponse("Forbidden", "Access denied to the requested resource."));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Unauthorized", "Invalid or missing token."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Internal Server Error", "An unexpected error occurred."));
        }
    }

    /**
     * Validates the provided JWT token and extracts the ophthalmologist ID from it.
     *
     * @param token the JWT token to be validated
     * @return the extracted ophthalmologist ID as a String
     * @throws UnauthorizedException if the token is invalid or the ophthalmologist ID is not found
     */
    private String validateToken(String token) {
        String ophtalIdString = jwtUtil.extractIdFromToken(token);
        if (ophtalIdString == null) {
            throw new UnauthorizedException("Invalid Token or ophtalmologist ID not found.");
        }
        return ophtalIdString;
    }

}
