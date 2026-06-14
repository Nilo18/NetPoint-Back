package com.netpoint.main.services;

import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.exceptions.PlanLimitExceededException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.ProductRepository;
import com.netpoint.main.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanEnforcementServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PlanEnforcementService planEnforcementService;

    private Company starterCompany;
    private Company professionalCompany;
    private Company businessPlusCompany;

    @BeforeEach
    void setUp() {
        PaymentPlan starterPlan = new PaymentPlan();
        starterPlan.setPlanName("Starter Plan");

        PaymentPlan professionalPlan = new PaymentPlan();
        professionalPlan.setPlanName("Professional Plan");

        PaymentPlan businessPlusPlan = new PaymentPlan();
        businessPlusPlan.setPlanName("Business Plus Plan");

        starterCompany = new Company();
        starterCompany.setId(1);
        starterCompany.setPlan(starterPlan);

        professionalCompany = new Company();
        professionalCompany.setId(2);
        professionalCompany.setPlan(professionalPlan);

        businessPlusCompany = new Company();
        businessPlusCompany.setId(3);
        businessPlusCompany.setPlan(businessPlusPlan);
    }

    // ─── enforceProductLimit ─────────────────────────────────────────

    @Test
    void enforceProductLimit_starterUnderLimit_passes() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(productRepository.countByCompany_Id(1)).thenReturn(99L);

        assertDoesNotThrow(() -> planEnforcementService.enforceProductLimit(1));
    }

    @Test
    void enforceProductLimit_starterAtLimit_throws() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(productRepository.countByCompany_Id(1)).thenReturn(100L);

        assertThrows(PlanLimitExceededException.class,
                () -> planEnforcementService.enforceProductLimit(1));
    }

    @Test
    void enforceProductLimit_professional_alwaysPasses() {
        when(companyRepository.findById(2)).thenReturn(Optional.of(professionalCompany));

        assertDoesNotThrow(() -> planEnforcementService.enforceProductLimit(2));
        verifyNoInteractions(productRepository);
    }

    @Test
    void enforceProductLimit_businessPlus_alwaysPasses() {
        when(companyRepository.findById(3)).thenReturn(Optional.of(businessPlusCompany));

        assertDoesNotThrow(() -> planEnforcementService.enforceProductLimit(3));
        verifyNoInteractions(productRepository);
    }

    @Test
    void enforceProductLimit_companyNotFound_throws() {
        when(companyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> planEnforcementService.enforceProductLimit(99));
    }

    // ─── enforceTeamMemberLimit — Starter ────────────────────────────

    @Test
    void enforceTeamMemberLimit_starterUnderTotalLimit_passes() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(userRepository.countByCompany_Id(1)).thenReturn(4L);
        when(userRepository.countByCompany_IdAndRoleIgnoreCase(1, "ADMIN")).thenReturn(1L);

        assertDoesNotThrow(() -> planEnforcementService.enforceTeamMemberLimit(1, "ADMIN"));
    }

    @Test
    void enforceTeamMemberLimit_starterTotalLimitReached_throws() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(userRepository.countByCompany_Id(1)).thenReturn(5L);

        assertThrows(PlanLimitExceededException.class,
                () -> planEnforcementService.enforceTeamMemberLimit(1, "ADMIN"));
    }

    @Test
    void enforceTeamMemberLimit_starterAdminLimitReached_throws() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(userRepository.countByCompany_Id(1)).thenReturn(3L);
        when(userRepository.countByCompany_IdAndRoleIgnoreCase(1, "ADMIN")).thenReturn(2L);

        assertThrows(PlanLimitExceededException.class,
                () -> planEnforcementService.enforceTeamMemberLimit(1, "ADMIN"));
    }

    @Test
    void enforceTeamMemberLimit_starterCashierLimitReached_throws() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(userRepository.countByCompany_Id(1)).thenReturn(4L);
        when(userRepository.countByCompany_IdAndRoleIgnoreCase(1, "CASHIER")).thenReturn(3L);

        assertThrows(PlanLimitExceededException.class,
                () -> planEnforcementService.enforceTeamMemberLimit(1, "CASHIER"));
    }

    @Test
    void enforceTeamMemberLimit_starterCashierUnderLimit_passes() {
        when(companyRepository.findById(1)).thenReturn(Optional.of(starterCompany));
        when(userRepository.countByCompany_Id(1)).thenReturn(3L);
        when(userRepository.countByCompany_IdAndRoleIgnoreCase(1, "CASHIER")).thenReturn(2L);

        assertDoesNotThrow(() -> planEnforcementService.enforceTeamMemberLimit(1, "CASHIER"));
    }

    // ─── enforceTeamMemberLimit — Professional ────────────────────────

    @Test
    void enforceTeamMemberLimit_professionalUnderLimit_passes() {
        when(companyRepository.findById(2)).thenReturn(Optional.of(professionalCompany));
        when(userRepository.countByCompany_Id(2)).thenReturn(9L);

        assertDoesNotThrow(() -> planEnforcementService.enforceTeamMemberLimit(2, "ADMIN"));
    }

    @Test
    void enforceTeamMemberLimit_professionalAtLimit_throws() {
        when(companyRepository.findById(2)).thenReturn(Optional.of(professionalCompany));
        when(userRepository.countByCompany_Id(2)).thenReturn(10L);

        assertThrows(PlanLimitExceededException.class,
                () -> planEnforcementService.enforceTeamMemberLimit(2, "ADMIN"));
    }

    // ─── enforceTeamMemberLimit — Business Plus ───────────────────────

    @Test
    void enforceTeamMemberLimit_businessPlus_alwaysPasses() {
        when(companyRepository.findById(3)).thenReturn(Optional.of(businessPlusCompany));

        assertDoesNotThrow(() -> planEnforcementService.enforceTeamMemberLimit(3, "ADMIN"));
        verifyNoInteractions(userRepository);
    }

    // ─── companyNotFound ──────────────────────────────────────────────

    @Test
    void enforceTeamMemberLimit_companyNotFound_throws() {
        when(companyRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(CompanyNotFoundException.class,
                () -> planEnforcementService.enforceTeamMemberLimit(99, "ADMIN"));
    }
}
