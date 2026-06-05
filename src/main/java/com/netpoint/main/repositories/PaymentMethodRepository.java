package com.netpoint.main.repositories;

import com.netpoint.main.models.PaymentMethod;
import com.netpoint.main.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {

    Optional<PaymentMethod> findByCompanyAndStatus(Company company, String status);

    boolean existsByCompanyAndStatus(Company company, String status);
}