package org.revature.repository;

import org.revature.entity.PKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductKeyRepository extends JpaRepository<PKey, Long> {
}
