# Tutorial: Implementing Division Hierarchy in OKR Tracking System

## Overview

This tutorial will guide you through restructuring your organizational hierarchy from:

**Current:** `Department → Users (Employees)`

**New:** `Division → Department → Users (Employees)`

---

## Step 1: Create the Division Entity

Create a new file: `src/main/java/uz/garantbank/okrTrackingSystem/entity/Division.java`

```java
package uz.garantbank.okrTrackingSystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "division")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"departments", "divisionLeader"})
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * Division leader (user with appropriate role)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User divisionLeader;

    /**
     * Departments belonging to this division
     */
    @OneToMany(mappedBy = "division", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private Set<Department> departments = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## Step 2: Update the Department Entity

Modify `src/main/java/uz/garantbank/okrTrackingSystem/entity/Department.java` to add the relationship to Division:

```java
@Entity
@Table(name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"assignedUsers", "departmentLeader", "objectives", "division"})
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    // NEW: Add this field for the Division relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private Division division;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Objective> objectives = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User departmentLeader;

    @ManyToMany(mappedBy = "assignedDepartments", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<User> assignedUsers = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## Step 3: Create Division Repository

Create `src/main/java/uz/garantbank/okrTrackingSystem/repository/DivisionRepository.java`:

```java
package uz.garantbank.okrTrackingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.garantbank.okrTrackingSystem.entity.Division;

import java.util.Optional;
import java.util.List;

@Repository
public interface DivisionRepository extends JpaRepository<Division, String> {

    Optional<Division> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT d FROM Division d LEFT JOIN FETCH d.departments WHERE d.id = :id")
    Optional<Division> findByIdWithDepartments(String id);

    @Query("SELECT d FROM Division d LEFT JOIN FETCH d.divisionLeader")
    List<Division> findAllWithLeaders();
}
```

---

## Step 4: Create Division DTOs

### DivisionDTO.java

Create `src/main/java/uz/garantbank/okrTrackingSystem/dto/DivisionDTO.java`:

```java
package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DivisionDTO {
    private String id;
    private String name;
    private String description;
    private UUID divisionLeaderId;
    private String divisionLeaderName;
    private Set<DepartmentSummaryDTO> departments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CreateDivisionRequest.java

Create `src/main/java/uz/garantbank/okrTrackingSystem/dto/division/CreateDivisionRequest.java`:

```java
package uz.garantbank.okrTrackingSystem.dto.division;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDivisionRequest {

    @NotBlank(message = "Division name is required")
    private String name;

    private String description;

    private UUID divisionLeaderId;
}
```

### UpdateDivisionRequest.java

Create `src/main/java/uz/garantbank/okrTrackingSystem/dto/division/UpdateDivisionRequest.java`:

```java
package uz.garantbank.okrTrackingSystem.dto.division;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDivisionRequest {
    private String name;
    private String description;
    private UUID divisionLeaderId;
}
```

### DepartmentSummaryDTO.java (Update if exists)

Update or create `src/main/java/uz/garantbank/okrTrackingSystem/dto/DepartmentSummaryDTO.java`:

```java
package uz.garantbank.okrTrackingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSummaryDTO {
    private String id;
    private String name;
    private String divisionId;
    private String divisionName;
    private UUID departmentLeaderId;
    private String departmentLeaderName;
    private int employeeCount;
}
```

---

## Step 5: Create Division Service

Create `src/main/java/uz/garantbank/okrTrackingSystem/service/DivisionService.java`:

```java
package uz.garantbank.okrTrackingSystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.garantbank.okrTrackingSystem.dto.DivisionDTO;
import uz.garantbank.okrTrackingSystem.dto.DepartmentSummaryDTO;
import uz.garantbank.okrTrackingSystem.dto.division.CreateDivisionRequest;
import uz.garantbank.okrTrackingSystem.dto.division.UpdateDivisionRequest;
import uz.garantbank.okrTrackingSystem.entity.Division;
import uz.garantbank.okrTrackingSystem.entity.User;
import uz.garantbank.okrTrackingSystem.exception.ResourceNotFoundException;
import uz.garantbank.okrTrackingSystem.repository.DivisionRepository;
import uz.garantbank.okrTrackingSystem.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final UserRepository userRepository;

    public List<DivisionDTO> getAllDivisions() {
        return divisionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DivisionDTO getDivisionById(String id) {
        Division division = divisionRepository.findByIdWithDepartments(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + id));
        return convertToDTO(division);
    }

    @Transactional
    public DivisionDTO createDivision(CreateDivisionRequest request) {
        if (divisionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Division with name '" + request.getName() + "' already exists");
        }

        Division division = Division.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        if (request.getDivisionLeaderId() != null) {
            User leader = userRepository.findById(request.getDivisionLeaderId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            division.setDivisionLeader(leader);
        }

        division = divisionRepository.save(division);
        return convertToDTO(division);
    }

    @Transactional
    public DivisionDTO updateDivision(String id, UpdateDivisionRequest request) {
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + id));

        if (request.getName() != null && !request.getName().equals(division.getName())) {
            if (divisionRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Division with name '" + request.getName() + "' already exists");
            }
            division.setName(request.getName());
        }

        if (request.getDescription() != null) {
            division.setDescription(request.getDescription());
        }

        if (request.getDivisionLeaderId() != null) {
            User leader = userRepository.findById(request.getDivisionLeaderId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            division.setDivisionLeader(leader);
        }

        division = divisionRepository.save(division);
        return convertToDTO(division);
    }

    @Transactional
    public void deleteDivision(String id) {
        Division division = divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Division not found with id: " + id));

        // Check if division has departments
        if (!division.getDepartments().isEmpty()) {
            throw new IllegalStateException("Cannot delete division with existing departments. Please reassign or delete departments first.");
        }

        divisionRepository.delete(division);
    }

    private DivisionDTO convertToDTO(Division division) {
        return DivisionDTO.builder()
                .id(division.getId())
                .name(division.getName())
                .description(division.getDescription())
                .divisionLeaderId(division.getDivisionLeader() != null ? division.getDivisionLeader().getId() : null)
                .divisionLeaderName(division.getDivisionLeader() != null ? division.getDivisionLeader().getFullName() : null)
                .departments(division.getDepartments().stream()
                        .map(dept -> DepartmentSummaryDTO.builder()
                                .id(dept.getId())
                                .name(dept.getName())
                                .divisionId(division.getId())
                                .divisionName(division.getName())
                                .departmentLeaderId(dept.getDepartmentLeader() != null ? dept.getDepartmentLeader().getId() : null)
                                .departmentLeaderName(dept.getDepartmentLeader() != null ? dept.getDepartmentLeader().getFullName() : null)
                                .employeeCount(dept.getAssignedUsers().size())
                                .build())
                        .collect(Collectors.toSet()))
                .createdAt(division.getCreatedAt())
                .updatedAt(division.getUpdatedAt())
                .build();
    }
}
```

---

## Step 6: Create Division Controller

Create `src/main/java/uz/garantbank/okrTrackingSystem/controller/DivisionController.java`:

```java
package uz.garantbank.okrTrackingSystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.garantbank.okrTrackingSystem.dto.DivisionDTO;
import uz.garantbank.okrTrackingSystem.dto.division.CreateDivisionRequest;
import uz.garantbank.okrTrackingSystem.dto.division.UpdateDivisionRequest;
import uz.garantbank.okrTrackingSystem.service.DivisionService;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionService divisionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<List<DivisionDTO>> getAllDivisions() {
        return ResponseEntity.ok(divisionService.getAllDivisions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<DivisionDTO> getDivisionById(@PathVariable String id) {
        return ResponseEntity.ok(divisionService.getDivisionById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DivisionDTO> createDivision(@Valid @RequestBody CreateDivisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(divisionService.createDivision(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DivisionDTO> updateDivision(
            @PathVariable String id,
            @Valid @RequestBody UpdateDivisionRequest request) {
        return ResponseEntity.ok(divisionService.updateDivision(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDivision(@PathVariable String id) {
        divisionService.deleteDivision(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Step 7: Update Department Service

Modify your `DepartmentService` to include division handling. Add these methods:

```java
@Transactional
public DepartmentDTO assignDivision(String departmentId, String divisionId) {
    Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

    Division division = divisionRepository.findById(divisionId)
            .orElseThrow(() -> new ResourceNotFoundException("Division not found"));

    department.setDivision(division);
    department = departmentRepository.save(department);

    return convertToDTO(department);
}

@Transactional
public DepartmentDTO removeDivision(String departmentId) {
    Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

    department.setDivision(null);
    department = departmentRepository.save(department);

    return convertToDTO(department);
}
```

Update the `convertToDTO` method to include division information:

```java
private DepartmentDTO convertToDTO(Department department) {
    return DepartmentDTO.builder()
            .id(department.getId())
            .name(department.getName())
            .divisionId(department.getDivision() != null ? department.getDivision().getId() : null)
            .divisionName(department.getDivision() != null ? department.getDivision().getName() : null)
            // ... rest of the fields
            .build();
}
```

---

## Step 8: Update Department DTOs

Update `src/main/java/uz/garantbank/okrTrackingSystem/dto/DepartmentDTO.java`:

Add these fields:

```java
private String divisionId;
private String divisionName;
```

Update `CreateDepartmentRequest` to include divisionId:

```java
private String divisionId; // Optional: division to assign to
```

---

## Step 9: Update Database Schema

You have two options:

### Option A: Using Liquibase/Flyway (Recommended)

Create a migration file (e.g., `V2__add_division_table.sql`):

```sql
-- Create division table
CREATE TABLE division (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    leader_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (leader_id) REFERENCES users(id)
);

-- Add division_id to department table
ALTER TABLE department ADD COLUMN division_id VARCHAR(36);
ALTER TABLE department ADD CONSTRAINT fk_department_division
    FOREIGN KEY (division_id) REFERENCES division(id);
```

### Option B: Let JPA Create Tables (For Development)

Update `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Then restart the application. Hibernate will automatically create the division table and add the division_id column to the department table.

---

## Step 10: Update Role Enum (Optional)

If you want to add a DIVISION_LEADER role, update your `Role` enum:

```java
public enum Role {
    ADMIN,
    HR,
    DIVISION_LEADER,  // NEW
    DEPARTMENT_LEADER,
    EMPLOYEE
}
```

---

## Step 11: Testing the Implementation

### Test 1: Create a Division

```bash
POST /api/divisions
Content-Type: application/json

{
  "name": "Information Technology Division",
  "description": "Manages all IT departments",
  "divisionLeaderId": "user-uuid-here"
}
```

### Test 2: Create a Department with Division

```bash
POST /api/departments
Content-Type: application/json

{
  "name": "Software Development Department",
  "divisionId": "division-uuid-here"
}
```

### Test 3: Get All Divisions with Departments

```bash
GET /api/divisions
```

### Test 4: Assign Department to Division

```bash
PUT /api/departments/{departmentId}/division/{divisionId}
```

---

## Step 12: Update Frontend (If Applicable)

If you have a frontend application:

1. **Add Division Management UI**
   - Division list page
   - Create/Edit division forms
   - Division detail page showing departments

2. **Update Department Forms**
   - Add division dropdown selector
   - Display division name in department lists

3. **Update Navigation**
   - Add "Divisions" menu item
   - Update breadcrumbs: Division > Department > Employee

---

## Step 13: Data Migration (For Existing Data)

If you have existing departments without divisions, you may want to:

1. Create a default division:

```java
Division defaultDivision = Division.builder()
    .name("General Division")
    .description("Default division for existing departments")
    .build();
divisionRepository.save(defaultDivision);
```

2. Assign all existing departments to the default division:

```java
List<Department> departments = departmentRepository.findAll();
for (Department dept : departments) {
    if (dept.getDivision() == null) {
        dept.setDivision(defaultDivision);
    }
}
departmentRepository.saveAll(departments);
```

---

## Final Hierarchy Structure

```
Organization
│
├── Division 1 (e.g., "IT Division")
│   ├── Department 1.1 (e.g., "Software Development")
│   │   ├── Employee 1
│   │   ├── Employee 2
│   │   └── Employee 3
│   └── Department 1.2 (e.g., "Infrastructure")
│       ├── Employee 4
│       └── Employee 5
│
├── Division 2 (e.g., "Finance Division")
│   └── Department 2.1 (e.g., "Accounting")
│       ├── Employee 6
│       └── Employee 7
│
└── Division 3 (e.g., "HR Division")
    └── Department 3.1 (e.g., "Recruitment")
        └── Employee 8
```

---

## Summary Checklist

- [ ] Create Division entity
- [ ] Update Department entity (add division relationship)
- [ ] Create DivisionRepository
- [ ] Create Division DTOs
- [ ] Create DivisionService
- [ ] Create DivisionController
- [ ] Update DepartmentService
- [ ] Update DepartmentDTO
- [ ] Update database schema
- [ ] Test all endpoints
- [ ] Migrate existing data (if applicable)
- [ ] Update frontend (if applicable)

---

## Additional Considerations

1. **Cascade Operations**: Decide what happens when a division is deleted
   - Currently set to prevent deletion if departments exist
   - Could change to reassign departments or cascade delete

2. **Security**: Ensure proper role-based access control
   - Only ADMIN can create/update/delete divisions
   - DIVISION_LEADER can view their division's data

3. **Validation**: Add business rules
   - Department must belong to exactly one division (or nullable)
   - Division names must be unique

4. **Reporting**: Update reporting queries to include division level
   - Division-level OKR aggregation
   - Division performance metrics

---

Good luck with your implementation! If you encounter any issues, feel free to ask for help.
