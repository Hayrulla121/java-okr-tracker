package uz.garantbank.okrTrackingSystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.garantbank.okrTrackingSystem.entity.Objective;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, String> {
    List<Objective> findByDepartmentId(String departmentId);

    /**
     * Find objectives by department ID with key results eagerly loaded
     */
    @Query("SELECT DISTINCT o FROM Objective o LEFT JOIN FETCH o.keyResults WHERE o.department.id = :departmentId")
    List<Objective> findByDepartmentIdWithKeyResults(@Param("departmentId") String departmentId);

    /**
     * Find an objective by ID with key results eagerly loaded
     */
    @Query("SELECT o FROM Objective o LEFT JOIN FETCH o.keyResults WHERE o.id = :id")
    Optional<Objective> findByIdWithKeyResults(@Param("id") String id);
}