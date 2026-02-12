package uz.garantbank.okrTrackingSystem.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.garantbank.okrTrackingSystem.dto.DepartmentDTO;
import uz.garantbank.okrTrackingSystem.dto.DepartmentScoreResult;
import uz.garantbank.okrTrackingSystem.dto.KeyResultDTO;
import uz.garantbank.okrTrackingSystem.dto.ObjectiveDTO;
import uz.garantbank.okrTrackingSystem.entity.User;
import uz.garantbank.okrTrackingSystem.repository.KeyResultRepository;
import uz.garantbank.okrTrackingSystem.repository.ObjectiveRepository;
import uz.garantbank.okrTrackingSystem.service.DepartmentAccessService;
import uz.garantbank.okrTrackingSystem.service.ExcelExportService;
import uz.garantbank.okrTrackingSystem.service.OkrService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class OkrController {

    private final OkrService okrService;
    private final ExcelExportService excelExportService;
    private final DepartmentAccessService accessService;
    private final ObjectiveRepository objectiveRepository;
    private final KeyResultRepository keyResultRepository;

    // ==================== DEPARTMENTS ====================

    @Tag(name = "Departments")
    @Operation(summary = "Get all departments", description = "Returns all departments with their objectives, key results, and computed scores. Accessible to all authenticated users.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all departments",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DepartmentDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content)
    })
    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        return ResponseEntity.ok(okrService.getAllDepartments());
    }

    @Tag(name = "Departments")
    @Operation(summary = "Get department by ID", description = "Returns a single department with its objectives, key results, and computed scores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department found",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentDTO> getDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(okrService.getDepartment(id));
    }

    @Tag(name = "Departments")
    @Operation(summary = "Create department", description = "Create a new department. **Requires ADMIN or DIRECTOR role.** " +
            "The department must be assigned to an existing division via the `divisionId` field.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department created",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions", content = @Content)
    })
    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(okrService.createDepartment(dto));
    }

    @Tag(name = "Departments")
    @Operation(summary = "Update department", description = "Update a department's name and other properties. " +
            "Requires edit permission for the department (ADMIN, DIRECTOR, or assigned DEPARTMENT_LEADER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated",
                    content = @Content(schema = @Schema(implementation = DepartmentDTO.class))),
            @ApiResponse(responseCode = "403", description = "No edit permission for this department", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable String id,
            @RequestBody DepartmentDTO dto) {
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, id)) {
            throw new AccessDeniedException("You do not have permission to edit this department");
        }
        return ResponseEntity.ok(okrService.updateDepartment(id, dto));
    }

    @Tag(name = "Departments")
    @Operation(summary = "Delete department", description = "Permanently delete a department and all its objectives/key results. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Department deleted"),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can delete departments", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable String id) {
        okrService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "Departments")
    @Operation(summary = "Get department scores with evaluations",
            description = "Returns detailed score breakdown for a department including automatic OKR score (60%), " +
                    "Director evaluation (20%), HR evaluation (20%), Business Block assessment, and the combined final score.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department score details",
                    content = @Content(schema = @Schema(implementation = DepartmentScoreResult.class))),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    @GetMapping("/departments/{id}/scores")
    public ResponseEntity<DepartmentScoreResult> getDepartmentScores(
            @Parameter(description = "Department ID", required = true) @PathVariable String id) {
        return ResponseEntity.ok(okrService.getDepartmentScoreWithEvaluations(id));
    }

    // ==================== OBJECTIVES ====================

    @Tag(name = "Objectives")
    @Operation(summary = "Create objective", description = "Create a new objective within a department. " +
            "The objective weight should be between 0-100 and all objective weights in a department should sum to 100%. " +
            "Requires edit permission for the department.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Objective created",
                    content = @Content(schema = @Schema(implementation = ObjectiveDTO.class))),
            @ApiResponse(responseCode = "403", description = "No edit permission for this department", content = @Content),
            @ApiResponse(responseCode = "404", description = "Department not found", content = @Content)
    })
    @PostMapping("/departments/{departmentId}/objectives")
    public ResponseEntity<ObjectiveDTO> createObjective(
            @Parameter(description = "Department ID", required = true) @PathVariable String departmentId,
            @RequestBody ObjectiveDTO dto) {
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to create objectives in this department");
        }
        return ResponseEntity.ok(okrService.createObjective(departmentId, dto));
    }

    @Tag(name = "Objectives")
    @Operation(summary = "Update objective", description = "Update an objective's name and weight. Requires edit permission for the parent department.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Objective updated",
                    content = @Content(schema = @Schema(implementation = ObjectiveDTO.class))),
            @ApiResponse(responseCode = "403", description = "No edit permission", content = @Content),
            @ApiResponse(responseCode = "404", description = "Objective not found", content = @Content)
    })
    @PutMapping("/objectives/{id}")
    public ResponseEntity<ObjectiveDTO> updateObjective(
            @Parameter(description = "Objective ID", required = true) @PathVariable String id,
            @RequestBody ObjectiveDTO dto) {
        String departmentId = getDepartmentIdFromObjective(id);
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to edit objectives in this department");
        }
        return ResponseEntity.ok(okrService.updateObjective(id, dto));
    }

    @Tag(name = "Objectives")
    @Operation(summary = "Delete objective", description = "Delete an objective and all its key results. Requires edit permission for the parent department.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Objective deleted"),
            @ApiResponse(responseCode = "403", description = "No edit permission", content = @Content),
            @ApiResponse(responseCode = "404", description = "Objective not found", content = @Content)
    })
    @DeleteMapping("/objectives/{id}")
    public ResponseEntity<Void> deleteObjective(
            @Parameter(description = "Objective ID", required = true) @PathVariable String id) {
        String departmentId = getDepartmentIdFromObjective(id);
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to delete objectives in this department");
        }
        okrService.deleteObjective(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== KEY RESULTS ====================

    @Tag(name = "Key Results")
    @Operation(summary = "Create key result", description = "Create a new key result within an objective. " +
            "Configure metric type (HIGHER_BETTER, LOWER_BETTER, QUALITATIVE), unit, weight, and threshold values. " +
            "Key result weights within an objective should sum to 100%.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Key result created",
                    content = @Content(schema = @Schema(implementation = KeyResultDTO.class))),
            @ApiResponse(responseCode = "403", description = "No edit permission", content = @Content),
            @ApiResponse(responseCode = "404", description = "Objective not found", content = @Content)
    })
    @PostMapping("/objectives/{objectiveId}/key-results")
    public ResponseEntity<KeyResultDTO> createKeyResult(
            @Parameter(description = "Objective ID", required = true) @PathVariable String objectiveId,
            @RequestBody KeyResultDTO dto) {
        String departmentId = getDepartmentIdFromObjective(objectiveId);
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to create key results in this department");
        }
        return ResponseEntity.ok(okrService.createKeyResult(objectiveId, dto));
    }

    @Tag(name = "Key Results")
    @Operation(summary = "Update key result", description = "Update a key result's properties including name, description, metric type, thresholds, and weight.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Key result updated",
                    content = @Content(schema = @Schema(implementation = KeyResultDTO.class))),
            @ApiResponse(responseCode = "403", description = "No edit permission", content = @Content),
            @ApiResponse(responseCode = "404", description = "Key result not found", content = @Content)
    })
    @PutMapping("/key-results/{id}")
    public ResponseEntity<KeyResultDTO> updateKeyResult(
            @Parameter(description = "Key Result ID", required = true) @PathVariable String id,
            @RequestBody KeyResultDTO dto) {
        String departmentId = getDepartmentIdFromKeyResult(id);
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to edit key results in this department");
        }
        return ResponseEntity.ok(okrService.updateKeyResult(id, dto));
    }

    @Tag(name = "Key Results")
    @Operation(summary = "Update key result actual value",
            description = "Update only the actual/measured value of a key result. " +
                    "The score is automatically recalculated based on the new value and the configured thresholds.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actual value updated and score recalculated",
                    content = @Content(schema = @Schema(implementation = KeyResultDTO.class))),
            @ApiResponse(responseCode = "403", description = "No edit permission", content = @Content),
            @ApiResponse(responseCode = "404", description = "Key result not found", content = @Content)
    })
    @PutMapping("/key-results/{id}/actual-value")
    public ResponseEntity<KeyResultDTO> updateKeyResultActualValue(
            @Parameter(description = "Key Result ID", required = true) @PathVariable String id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Map with key `actualValue` containing the new measured value",
                    content = @Content(schema = @Schema(example = "{\"actualValue\": \"95.5\"}"))
            )
            @RequestBody java.util.Map<String, String> payload) {
        String departmentId = getDepartmentIdFromKeyResult(id);
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to edit key results in this department");
        }
        return ResponseEntity.ok(okrService.updateKeyResultActualValue(id, payload.get("actualValue")));
    }

    @Tag(name = "Key Results")
    @Operation(summary = "Delete key result", description = "Permanently delete a key result. Requires edit permission for the parent department.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Key result deleted"),
            @ApiResponse(responseCode = "403", description = "No edit permission", content = @Content),
            @ApiResponse(responseCode = "404", description = "Key result not found", content = @Content)
    })
    @DeleteMapping("/key-results/{id}")
    public ResponseEntity<Void> deleteKeyResult(
            @Parameter(description = "Key Result ID", required = true) @PathVariable String id) {
        String departmentId = getDepartmentIdFromKeyResult(id);
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to delete key results in this department");
        }
        okrService.deleteKeyResult(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== EXPORT ====================

    @Tag(name = "Export")
    @Operation(summary = "Export OKRs to Excel",
            description = "Export all departments, objectives, and key results to an Excel (.xlsx) file. " +
                    "The file includes scores and status information. **Public endpoint — no authentication required.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel file generated",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "500", description = "Export failed", content = @Content)
    })
    @io.swagger.v3.oas.annotations.security.SecurityRequirements // Public endpoint
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel() {
        try {
            List<DepartmentDTO> departments = okrService.getAllDepartments();
            byte[] excelData = excelExportService.exportToExcel(departments);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "okr_export.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            System.err.println("Excel export failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== DEMO DATA ====================

    @Tag(name = "Demo Data")
    @Operation(summary = "Load demo data", description = "Load sample OKR data for testing and demonstration purposes. " +
            "Creates sample divisions, departments, objectives, and key results. **Requires ADMIN role.**")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demo data loaded — returns all departments",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DepartmentDTO.class)))),
            @ApiResponse(responseCode = "403", description = "Only ADMIN can load demo data", content = @Content)
    })
    @PostMapping("/demo/load")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentDTO>> loadDemoData() {
        return ResponseEntity.ok(okrService.loadDemoData());
    }

    // ==================== HELPER METHODS ====================

    private String getDepartmentIdFromObjective(String objectiveId) {
        return objectiveRepository.findById(objectiveId)
                .map(obj -> obj.getDepartment().getId())
                .orElseThrow(() -> new EntityNotFoundException("Objective not found: " + objectiveId));
    }

    private String getDepartmentIdFromKeyResult(String keyResultId) {
        return keyResultRepository.findById(keyResultId)
                .map(kr -> kr.getObjective().getDepartment().getId())
                .orElseThrow(() -> new EntityNotFoundException("Key Result not found: " + keyResultId));
    }
}
