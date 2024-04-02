package org.revature.repository;

import org.revature.entity.Canonical;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CanonicalRepository extends JpaRepository<Canonical, Long> {
    Canonical findByName(String name);
}
