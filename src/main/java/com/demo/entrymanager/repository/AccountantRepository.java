package com.demo.entrymanager.repository;

import com.demo.entrymanager.model.Accountant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountantRepository extends JpaRepository<Accountant, Long> {
}
