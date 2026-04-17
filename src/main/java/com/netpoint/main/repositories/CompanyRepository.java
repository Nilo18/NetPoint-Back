package com.netpoint.main.repositories;

import com.netpoint.main.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByEmail(String email);

    List<Company> findAllByEmail(String mail);

    Company findByEmail(String mail);
}
