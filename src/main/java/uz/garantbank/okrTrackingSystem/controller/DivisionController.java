package uz.garantbank.okrTrackingSystem.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.garantbank.okrTrackingSystem.dto.CreateDivisionRequest;
import uz.garantbank.okrTrackingSystem.dto.DivisionDTO;
import uz.garantbank.okrTrackingSystem.dto.UpdateDivisionRequest;
import uz.garantbank.okrTrackingSystem.dto.user.DepartmentSummaryDTO;
import uz.garantbank.okrTrackingSystem.service.DivisionAccessService;
import uz.garantbank.okrTrackingSystem.service.DivisionService;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DivisionController {

    private final DivisionService divisionService;
    private final DivisionAccessService divisionAccessService;

    public DivisionController(
            DivisionService divisionService,
            DivisionAccessService divisionAccessService
    ) {
        this.divisionService = divisionService;
        this.divisionAccessService = divisionAccessService;
    }

    /**
     * GET /api/divisions
     * Get all divisions (all authenticated users)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DivisionDTO>> getAllDivisions() {
        List<DivisionDTO> divisions = divisionService.getAllDivisions();
        return ResponseEntity.ok(divisions);
    }

    /**
     * GET /api/divisions/{id}
     * Get single division by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DivisionDTO> getDivisionById(@PathVariable String id) {
        DivisionDTO division = divisionService.getDivisionById(id);
        return ResponseEntity.ok(division);
    }

    /**
     * POST /api/divisions
     * Create new division (ADMIN or DIRECTOR only)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    public ResponseEntity<DivisionDTO> createDivision(
            @Valid @RequestBody CreateDivisionRequest request
    ) {
        DivisionDTO created = divisionService.createDivision(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/divisions/{id}
     * Update division (requires edit permission)
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DivisionDTO> updateDivision(
            @PathVariable String id,
            @Valid @RequestBody UpdateDivisionRequest request,
            Authentication authentication
    ) {
        // Check if user can edit this division
        if (!divisionAccessService.canUserEditDivision(authentication, id)) {
            throw new AccessDeniedException("You don't have permission to edit this division");
        }

        DivisionDTO updated = divisionService.updateDivision(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/divisions/{id}
     * Delete division (ADMIN only, only if no departments)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDivision(@PathVariable String id) {
        divisionService.deleteDivision(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/divisions/{id}/departments
     * Get all departments in a division
     */
    @GetMapping("/{id}/departments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepartmentSummaryDTO>> getDepartmentsByDivision(
            @PathVariable String id
    ) {
        List<DepartmentSummaryDTO> departments =
                divisionService.getDepartmentsByDivisionId(id);
        return ResponseEntity.ok(departments);
    }
}