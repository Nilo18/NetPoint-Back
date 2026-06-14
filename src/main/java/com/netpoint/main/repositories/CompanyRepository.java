package com.netpoint.main.repositories;

import com.netpoint.main.models.Company;
import com.netpoint.main.models.Product;
import com.netpoint.main.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    boolean existsByEmail(String email);

    List<Company> findAllByEmail(String mail);

    Company findByEmail(String mail);
}
