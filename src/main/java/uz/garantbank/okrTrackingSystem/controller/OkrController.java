package uz.garantbank.okrTrackingSystem.controller;


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

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        // All authenticated users can view all departments
        return ResponseEntity.ok(okrService.getAllDepartments());
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentDTO> getDepartment(@PathVariable String id) {
        // All authenticated users can view any department
        return ResponseEntity.ok(okrService.getDepartment(id));
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<DepartmentDTO> createDepartment(@RequestBody DepartmentDTO dto) {
        // ADMIN and DIRECTOR can create departments
        return ResponseEntity.ok(okrService.createDepartment(dto));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable String id, @RequestBody DepartmentDTO dto) {
        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, id)) {
            throw new AccessDeniedException("You do not have permission to edit this department");
        }
        return ResponseEntity.ok(okrService.updateDepartment(id, dto));
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        // Only ADMIN can delete departments
        okrService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departments/{id}/scores")
    public ResponseEntity<DepartmentScoreResult> getDepartmentScores(@PathVariable String id) {
        // All authenticated users can view scores
        return ResponseEntity.ok(okrService.getDepartmentScoreWithEvaluations(id));
    }

    // ==================== OBJECTIVES ====================

    @PostMapping("/departments/{departmentId}/objectives")
    public ResponseEntity<ObjectiveDTO> createObjective(
            @PathVariable String departmentId, @RequestBody ObjectiveDTO dto) {
        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to create objectives in this department");
        }
        return ResponseEntity.ok(okrService.createObjective(departmentId, dto));
    }

    @PutMapping("/objectives/{id}")
    public ResponseEntity<ObjectiveDTO> updateObjective(
            @PathVariable String id, @RequestBody ObjectiveDTO dto) {
        // Get department ID from objective
        String departmentId = getDepartmentIdFromObjective(id);

        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to edit objectives in this department");
        }
        return ResponseEntity.ok(okrService.updateObjective(id, dto));
    }

    @DeleteMapping("/objectives/{id}")
    public ResponseEntity<Void> deleteObjective(@PathVariable String id) {
        // Get department ID from objective
        String departmentId = getDepartmentIdFromObjective(id);

        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to delete objectives in this department");
        }
        okrService.deleteObjective(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== KEY RESULTS ====================

    @PostMapping("/objectives/{objectiveId}/key-results")
    public ResponseEntity<KeyResultDTO> createKeyResult(
            @PathVariable String objectiveId, @RequestBody KeyResultDTO dto) {
        // Get department ID from objective
        String departmentId = getDepartmentIdFromObjective(objectiveId);

        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to create key results in this department");
        }
        return ResponseEntity.ok(okrService.createKeyResult(objectiveId, dto));
    }

    @PutMapping("/key-results/{id}")
    public ResponseEntity<KeyResultDTO> updateKeyResult(
            @PathVariable String id, @RequestBody KeyResultDTO dto) {
        // Get department ID from key result
        String departmentId = getDepartmentIdFromKeyResult(id);

        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to edit key results in this department");
        }
        return ResponseEntity.ok(okrService.updateKeyResult(id, dto));
    }

    @PutMapping("/key-results/{id}/actual-value")
    public ResponseEntity<KeyResultDTO> updateKeyResultActualValue(
            @PathVariable String id, @RequestBody java.util.Map<String, String> payload) {
        // Get department ID from key result
        String departmentId = getDepartmentIdFromKeyResult(id);

        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to edit key results in this department");
        }
        return ResponseEntity.ok(okrService.updateKeyResultActualValue(id, payload.get("actualValue")));
    }

    @DeleteMapping("/key-results/{id}")
    public ResponseEntity<Void> deleteKeyResult(@PathVariable String id) {
        // Get department ID from key result
        String departmentId = getDepartmentIdFromKeyResult(id);

        // Check if user can edit this department
        User currentUser = accessService.getCurrentUser();
        if (!accessService.canEditDepartment(currentUser, departmentId)) {
            throw new AccessDeniedException("You do not have permission to delete key results in this department");
        }
        okrService.deleteKeyResult(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== EXPORT ====================

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

    @PostMapping("/demo/load")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DepartmentDTO>> loadDemoData() {
        // Only ADMIN can load demo data
        return ResponseEntity.ok(okrService.loadDemoData());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get department ID from an objective ID
     */
    private String getDepartmentIdFromObjective(String objectiveId) {
        return objectiveRepository.findById(objectiveId)
                .map(obj -> obj.getDepartment().getId())
                .orElseThrow(() -> new EntityNotFoundException("Objective not found: " + objectiveId));
    }

    /**
     * Get department ID from a key result ID
     */
    private String getDepartmentIdFromKeyResult(String keyResultId) {
        return keyResultRepository.findById(keyResultId)
                .map(kr -> kr.getObjective().getDepartment().getId())
                .orElseThrow(() -> new EntityNotFoundException("Key Result not found: " + keyResultId));
    }
}