package com.netpoint.main.services;

import com.netpoint.main.dto.PaymentPlanDTO;
import com.netpoint.main.dto.responses.GenericResponse;
import com.netpoint.main.dto.responses.GenericResponse;
import com.netpoint.main.exceptions.BadRequestException;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.exceptions.PaymentPlanNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.PaymentPlanRepository;
import lombok.Data;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.netpoint.main.repositories.PaymentMethodRepository;
import java.util.List;

@Data
@Service
public class PaymentPlanService {
    private final PaymentPlanRepository paymentPlanRepository;
    private final CompanyRepository companyRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Cacheable(value = "paymentPlans", key = "#companyId")
    @Transactional(readOnly = true)
    public PaymentPlanDTO getPaymentPlan(Integer companyId) {
        Company company = companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        PaymentPlan paymentPlan = company.getPlan();

        if (paymentPlan == null) {
            throw new PaymentPlanNotFoundException("Payment plan not found");
        }

        return new PaymentPlanDTO(
                paymentPlan.getId(),
                paymentPlan.getPlanName(),
                paymentPlan.getPlanPurpose(),
                paymentPlan.getCostPerMonth(),
                paymentPlan.getPlanRules()
        );
    }

    @CacheEvict(value = "paymentPlans", key = "#companyId")
    @Transactional
public GenericResponse changePlan(Integer companyId, String newPlanName) {
    Company company = companyRepository.findById(Long.valueOf(companyId))
            .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

    PaymentPlan newPlan = paymentPlanRepository.findByPlanName(newPlanName)
            .orElseThrow(() -> new PaymentPlanNotFoundException("Plan not found: " + newPlanName));

    // Block upgrade to paid plan if no active payment method exists
    if (newPlan.getCostPerMonth() > 0
            && !paymentMethodRepository.existsByCompanyAndStatus(company, "active")) {
        throw new BadRequestException(
                "Add a payment method before upgrading to a paid plan.");
    }

    company.setPlan(newPlan);
    companyRepository.save(company);
    return new GenericResponse(200, "Plan changed to: " + newPlan.getPlanName());
}

    @CacheEvict(value = "paymentPlans", key = "#companyId")
    @Transactional
    public GenericResponse cancelSubscription(Integer companyId) {
        Company company = companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        PaymentPlan starterPlan = paymentPlanRepository.findByPlanName("Starter Plan")
                .orElseThrow(() -> new PaymentPlanNotFoundException("Default plan not found"));

        company.setPlan(starterPlan);
        companyRepository.save(company);
        return new GenericResponse(200, "Plan cancelled successfully.");
    }
}
