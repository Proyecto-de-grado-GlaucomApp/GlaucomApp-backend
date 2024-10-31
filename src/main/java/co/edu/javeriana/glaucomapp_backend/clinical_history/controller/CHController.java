/**
 * Controller class for handling clinical history related requests.
 * Provides endpoints for managing patients and exams.
 * 
 * Endpoints:
 * - POST /mobile/clinical_history/save/pacient: Save a new patient.
 * - GET /mobile/clinical_history/get/pacient: Retrieve a patient by ID.
 * - GET /mobile/clinical_history/get/pacients: Retrieve a list of patients for an ophthalmologist.
 * - DELETE /mobile/clinical_history/delete/pacient/{pacientId}: Delete a patient by ID.
 * - POST /mobile/clinical_history/save/exam: Save a new exam.
 * - GET /mobile/clinical_history/get/exams: Retrieve a list of exams for a patient.
 * - DELETE /mobile/clinical_history/delete/exam/{examId}: Delete an exam by ID.
 * - GET /mobile/clinical_history/get/exam: Retrieve an exam by ID.
 * 
 * Authorization:
 * All endpoints require an Authorization header with a valid JWT token.
 * 
 * Error Handling:
 * Handles various exceptions and returns appropriate HTTP status codes and error messages.
 * 
 * Dependencies:
 * - PacientService: Service for managing patients.
 * - ExamService: Service for managing exams.
 * - JwtUtil: Utility for extracting information from JWT tokens.
 * 
 * @param pacientService Service for managing patients.
 * @param examService Service for managing exams.
 * @param jwtUtil Utility for extracting information from JWT tokens.
 */
package co.edu.javeriana.glaucomapp_backend.clinical_history.controller;

import java.util.List;

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
import jakarta.persistence.EntityNotFoundException;

@RequestMapping("/mobile/clinical_history")
@RestController
public class CHController {

    private final PacientService pacientService;

    private final ExamService examService;

    
    private final JwtUtil jwtUtil;

    public CHController(PacientService pacientService, ExamService examService, JwtUtil jwtUtil) {
        this.pacientService = pacientService;
        this.examService = examService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Endpoint to save a pacient's information.
     *
     * @param token the authorization token from the request header
     * @param pacient the pacient information to be saved
     * @return a ResponseEntity indicating the result of the save operation
     */
    @PostMapping("/save/pacient")
    public ResponseEntity<?> savePacient(@RequestHeader("Authorization") String token,
                                              @RequestBody PacientRequest pacient) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            pacientService.savePacient(pacient, ophtalIdString);
            return ResponseEntity.ok("Pacient saved successfully");
        });
    }


    /**
     * Retrieves a patient's information based on the provided ID.
     *
     * @param token the authorization token from the request header
     * @param cedula the ID of the patient to retrieve
     * @return a ResponseEntity containing the patient's information if found, 
     *         or a 404 status with an error message if the patient is not found
     */
    @GetMapping("/get/pacient")
    public ResponseEntity<?> getPacient(@RequestHeader("Authorization") String token,
                                        @RequestParam String cedula) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            PacientResponse pacient = pacientService.getPacientById(ophtalIdString, cedula);
            if (pacient == null || pacient.PacinetId() == null) {
                return ResponseEntity.status(404).body("Pacient not found");
            }
            return ResponseEntity.ok(pacient);
        });
    }


    /**
     * Retrieves a list of patients associated with an ophthalmologist.
     *
     * @param token the authorization token from the request header
     * @param startIndex the starting index for pagination
     * @param endIndex the ending index for pagination
     * @return a ResponseEntity containing a list of PacientResponse objects
     */
    @GetMapping("/get/pacients")
    public ResponseEntity<?> getPacientsByOphtal(@RequestHeader("Authorization") String token,
                                                                     @RequestParam int startIndex, @RequestParam int endIndex) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            List<PacientResponse> pacients = pacientService.getPacientsByOphtal(ophtalIdString, startIndex, endIndex);
            return ResponseEntity.ok(pacients);
        });
    }

    /**
     * Deletes a patient record based on the provided patient ID.
     *
     * @param token the authorization token from the request header
     * @param pacientId the ID of the patient to be deleted
     * @return a ResponseEntity indicating the result of the delete operation
     */
    @DeleteMapping("/delete/pacient/{pacientId}")
    public ResponseEntity<?> deletePacient(@RequestHeader("Authorization") String token,
                                                @PathVariable String pacientId) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            pacientService.deletePacient(ophtalIdString, pacientId);
            return ResponseEntity.ok("Pacient deleted successfully");
        });
    }


    /**
     * Endpoint to save an exam.
     * 
     * @param token the authorization token from the request header
     * @param examRequest the request body containing exam details
     * @return a ResponseEntity indicating the result of the save operation
     */
    @PostMapping("save/exam")
    public ResponseEntity<?> saveExam(@RequestHeader("Authorization") String token,
                                           @RequestBody ExamRequest examRequest) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            examService.saveExam(ophtalIdString, examRequest);
            return ResponseEntity.ok("Exam saved successfully");
        });
    }


    /**
     * Retrieves a list of exams for a specific patient within a specified range.
     *
     * @param token the authorization token from the request header
     * @param pacientId the ID of the patient whose exams are to be retrieved
     * @param startIndex the starting index of the exams list to be retrieved
     * @param endIndex the ending index of the exams list to be retrieved
     * @return a ResponseEntity containing the list of exams for the specified patient
     */
    @GetMapping("get/exams")
    public ResponseEntity<?> getExamsByPacient(@RequestHeader("Authorization") String token,
                                                                 @RequestParam String pacientId,
                                                                 @RequestParam int startIndex, @RequestParam int endIndex) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            List<ExamsResponse> exams = examService.getExamsByPacient(ophtalIdString, pacientId, startIndex, endIndex);
            return ResponseEntity.ok(exams);
        });
    }


    /**
     * Deletes an exam based on the provided exam ID, patient ID, and authorization token.
     *
     * @param token the authorization token from the request header
     * @param examId the ID of the exam to be deleted
     * @param pacientId the ID of the patient associated with the exam
     * @return a ResponseEntity indicating the result of the delete operation
     */
    @DeleteMapping("delete/exam/{examId}")
    public ResponseEntity<?> deleteExam(@RequestHeader("Authorization") String token,
                                             @PathVariable String examId,
                                             @RequestParam String pacientId) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            examService.deleteExam(ophtalIdString, pacientId, examId);
            return ResponseEntity.ok("Exam deleted successfully");
        });
    }

    /**
     * Retrieves an exam based on the provided exam ID and patient ID.
     *
     * @param token the authorization token from the request header
     * @param examId the ID of the exam to retrieve
     * @param pacientId the ID of the patient associated with the exam
     * @return a ResponseEntity containing the exam details if found, or a 404 status if not found
     */
    @GetMapping("get/exam")
    public ResponseEntity<?> getExam(@RequestHeader("Authorization") String token,
                                     @RequestParam String examId,
                                     @RequestParam String pacientId) {
        String ophtalIdString = validateToken(token);
        return handleRequest(() -> {
            ExamRes exam = examService.getExamById(ophtalIdString, pacientId, examId);
            if (exam == null) {
                return ResponseEntity.status(404).body("Exam not found");
            }
            return ResponseEntity.ok(exam);
        });
    }

    /**
     * Validates the provided JWT token and extracts the ophthalmologist ID from it.
     *
     * @param token the JWT token to be validated
     * @return the ophthalmologist ID extracted from the token
     * @throws UnauthorizedException if the token is invalid or the ophthalmologist ID is not found
     */
    private String validateToken(String token) {
        String ophtalIdString = jwtUtil.extractIdFromToken(token);
        if (ophtalIdString == null) {
            throw new UnauthorizedException("Invalid Token or ophthalmologist ID not found.");
        }
        return ophtalIdString;
    }

    /**
     * Handles a request by executing the given supplier and returning an appropriate ResponseEntity based on the outcome.
     * 
     * @param <T> The type of the response body.
     * @param supplier The supplier that provides the ResponseEntity to be returned.
     * @return A ResponseEntity containing the result of the supplier execution or an appropriate error response.
     * @throws IllegalArgumentException if the supplier throws an IllegalArgumentException.
     * @throws AccessDeniedException if the supplier throws an AccessDeniedException.
     * @throws UnauthorizedException if the supplier throws an UnauthorizedException.
     * @throws EntityNotFoundException if the supplier throws an EntityNotFoundException.
     * @throws RuntimeException if the supplier throws a RuntimeException.
     * @throws Exception if the supplier throws any other Exception.
     */
    private <T> ResponseEntity<?> handleRequest(CheckedSupplier<ResponseEntity<T>> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Bad Request", e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(new ErrorResponse("Forbidden", e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Unauthorized", e.getMessage()));
        } catch(EntityNotFoundException e) {
            return ResponseEntity.status(404).body(new ErrorResponse("Not Found", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(new ErrorResponse("Bad Request", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Internal Server Error", "An unexpected error occurred."));
        }
    }

    /**
     * A functional interface that represents a supplier of results which can throw an exception.
     * This is a specialization of {@link java.util.function.Supplier} that allows for checked exceptions.
     *
     * @param <T> the type of results supplied by this supplier
     */
    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }

}
