package com.netpoint.main.services;

import com.netpoint.main.dto.PaymentPlanDTO;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.exceptions.PaymentPlanNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.PaymentPlanRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Data
@Service
public class PaymentPlanService {
    private final PaymentPlanRepository paymentPlanRepository;
    private final CompanyRepository companyRepository;

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
}
