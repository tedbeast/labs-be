package org.revature.repository;

import org.revature.entity.Saved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavedRepository extends JpaRepository<Saved, Long> {

    @Query("from Saved s where s.pKey.id = :pkey and s.canonical.name = :name")
    Saved getSpecificSavedLab(@Param("pkey") long pkey, @Param("name") String name);
}
