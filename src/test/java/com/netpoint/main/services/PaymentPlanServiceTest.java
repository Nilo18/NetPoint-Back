package com.netpoint.main.services;

import com.netpoint.main.dto.PaymentPlanDTO;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.exceptions.PaymentPlanNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.PaymentPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentPlanServiceTest {

    @Mock
    private PaymentPlanRepository paymentPlanRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private PaymentPlanService paymentPlanService;

    private Company company;
    private PaymentPlan starterPlan;
    private PaymentPlan professionalPlan;

    @BeforeEach
    void setUp() {
        starterPlan = new PaymentPlan();
        starterPlan.setId(1);
        starterPlan.setPlanName("Starter Plan");
        starterPlan.setCostPerMonth(0.0);
        starterPlan.setPlanPurpose("For small shops and solo entrepreneurs");
        starterPlan.setPlanRules(List.of(
                "Up to 100 products in catalog",
                "5 Team members (2 Admins + 3 Cashiers)",
                "Standard Sales Reports",
                "7-day Activity Log retention"
        ));

        professionalPlan = new PaymentPlan();
        professionalPlan.setId(2);
        professionalPlan.setPlanName("Professional Plan");
        professionalPlan.setCostPerMonth(49.0);
        professionalPlan.setPlanPurpose("For growing businesses with multiple staff");
        professionalPlan.setPlanRules(List.of(
                "Unlimited products",
                "Up to 10 Team members",
                "30-day Activity Log retention"
        ));

        company = new Company();
        company.setId(1);
        company.setPlan(professionalPlan);
    }

    // getPaymentPlan

    @Test
    void getPaymentPlan_success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        PaymentPlanDTO result = paymentPlanService.getPaymentPlan(1);

        assertEquals(2, result.id());
        assertEquals("Professional Plan", result.planName());
        assertEquals(49.0, result.costPerMonth());
        assertEquals("For growing businesses with multiple staff", result.planPurpose());
        assertEquals(3, result.planRules().size());
    }

    @Test
    void getPaymentPlan_companyNotFound_throws() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> paymentPlanService.getPaymentPlan(99));
    }

    @Test
    void getPaymentPlan_noPlanOnCompany_throws() {
        company.setPlan(null);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        assertThrows(PaymentPlanNotFoundException.class,
                () -> paymentPlanService.getPaymentPlan(1));
    }

    // changePlan

    @Test
    void changePlan_success() {
        PaymentPlan businessPlus = new PaymentPlan();
        businessPlus.setId(3);
        businessPlus.setPlanName("Business Plus Plan");
        businessPlus.setCostPerMonth(99.0);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentPlanRepository.findByPlanName("Business Plus Plan"))
                .thenReturn(Optional.of(businessPlus));

        paymentPlanService.changePlan(1, "Business Plus Plan");

        assertEquals("Business Plus Plan", company.getPlan().getPlanName());
        verify(companyRepository, times(1)).save(company);
    }

    @Test
    void changePlan_companyNotFound_throws() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> paymentPlanService.changePlan(99, "Business Plus Plan"));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void changePlan_planNotFound_throws() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentPlanRepository.findByPlanName("Nonexistent Plan"))
                .thenReturn(Optional.empty());

        assertThrows(PaymentPlanNotFoundException.class,
                () -> paymentPlanService.changePlan(1, "Nonexistent Plan"));

        verify(companyRepository, never()).save(any());
    }

    // cancelSubscription

    @Test
    void cancelSubscription_success() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentPlanRepository.findByPlanName("Starter Plan"))
                .thenReturn(Optional.of(starterPlan));

        paymentPlanService.cancelSubscription(1);

        assertEquals("Starter Plan", company.getPlan().getPlanName());
        verify(companyRepository, times(1)).save(company);
    }

    @Test
    void cancelSubscription_companyNotFound_throws() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> paymentPlanService.cancelSubscription(99));

        verify(companyRepository, never()).save(any());
    }

    @Test
    void cancelSubscription_starterPlanMissingFromDB_throws() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentPlanRepository.findByPlanName("Starter Plan"))
                .thenReturn(Optional.empty());

        assertThrows(PaymentPlanNotFoundException.class,
                () -> paymentPlanService.cancelSubscription(1));

        verify(companyRepository, never()).save(any());
    }
}