package com.netpoint.main.services;

import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.exceptions.PlanLimitExceededException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.ProductRepository;
import com.netpoint.main.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanEnforcementService {

    private static final String STARTER_PLAN = "Starter Plan";
    private static final String PROFESSIONAL_PLAN = "Professional Plan";
    private static final String BUSINESS_PLUS_PLAN = "Business Plus Plan";

    private static final int STARTER_MAX_PRODUCTS = 100;
    private static final int STARTER_MAX_TEAM_MEMBERS = 5;
    private static final int STARTER_MAX_ADMINS = 2;
    private static final int STARTER_MAX_CASHIERS = 3;
    private static final int PROFESSIONAL_MAX_TEAM_MEMBERS = 10;

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    public void verifyPlanLimitsCompliant(Integer companyId, PaymentPlan targetPlan) {
        String targetPlanName = targetPlan.getPlanName();


        if (BUSINESS_PLUS_PLAN.equals(targetPlanName)) {
            return;
        }

        long totalMembers = userRepository.countByCompany_Id(companyId);

        if (STARTER_PLAN.equals(targetPlanName)) {

            if (totalMembers > STARTER_MAX_TEAM_MEMBERS) {
                throw new PlanLimitExceededException(
                        "Cannot switch to Starter Plan. You currently have " + totalMembers +
                                " team members, but Starter Plan only allows a maximum of " + STARTER_MAX_TEAM_MEMBERS + ".");
            }


            long adminCount = userRepository.countByCompany_IdAndRoleIgnoreCase(companyId, "ADMIN");
            if (adminCount > STARTER_MAX_ADMINS) {
                throw new PlanLimitExceededException(
                        "Cannot switch to Starter Plan. You currently have " + adminCount +
                                " admins, but Starter Plan only allows a maximum of " + STARTER_MAX_ADMINS + ".");
            }

            long cashierCount = userRepository.countByCompany_IdAndRoleIgnoreCase(companyId, "CASHIER");
            if (cashierCount > STARTER_MAX_CASHIERS) {
                throw new PlanLimitExceededException(
                        "Cannot switch to Starter Plan. You currently have " + cashierCount +
                                " cashiers, but Starter Plan only allows a maximum of " + STARTER_MAX_CASHIERS + ".");
            }


            long productCount = productRepository.countByCompany_Id(companyId);
            if (productCount > STARTER_MAX_PRODUCTS) {
                throw new PlanLimitExceededException(
                        "Cannot switch to Starter Plan. You currently have " + productCount +
                                " products, but Starter Plan only allows a maximum of " + STARTER_MAX_PRODUCTS + ".");
            }
        }

        if (PROFESSIONAL_PLAN.equals(targetPlanName)) {

            if (totalMembers > PROFESSIONAL_MAX_TEAM_MEMBERS) {
                throw new PlanLimitExceededException(
                        "Cannot switch to Professional Plan. You currently have " + totalMembers +
                                " team members, but Professional Plan only allows a maximum of " + PROFESSIONAL_MAX_TEAM_MEMBERS + ".");
            }
        }
    }


    public void enforceProductLimit(Integer companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company with the given id was not found"));

        String planName = company.getPlan().getPlanName();

        if (!STARTER_PLAN.equals(planName)) {
            return;
        }

        long productCount = productRepository.countByCompany_Id(companyId);
        if (productCount >= STARTER_MAX_PRODUCTS) {
            throw new PlanLimitExceededException(
                    "Starter Plan allows a maximum of " + STARTER_MAX_PRODUCTS + " products");
        }
    }

    public void enforceTeamMemberLimit(Integer companyId, String role) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company with the given id was not found"));

        String planName = company.getPlan().getPlanName();

        if (BUSINESS_PLUS_PLAN.equals(planName)) {
            return;
        }

        long totalMembers = userRepository.countByCompany_Id(companyId);
        String normalizedRole = role.trim().toUpperCase();

        if (STARTER_PLAN.equals(planName)) {
            if (totalMembers >= STARTER_MAX_TEAM_MEMBERS) {
                throw new PlanLimitExceededException(
                        "Starter Plan allows a maximum of " + STARTER_MAX_TEAM_MEMBERS + " team members");
            }

            if ("ADMIN".equals(normalizedRole)) {
                long adminCount = userRepository.countByCompany_IdAndRoleIgnoreCase(companyId, "ADMIN");
                if (adminCount >= STARTER_MAX_ADMINS) {
                    throw new PlanLimitExceededException(
                            "Starter Plan allows a maximum of " + STARTER_MAX_ADMINS + " admins");
                }
            }

            if ("CASHIER".equals(normalizedRole)) {
                long cashierCount = userRepository.countByCompany_IdAndRoleIgnoreCase(companyId, "CASHIER");
                if (cashierCount >= STARTER_MAX_CASHIERS) {
                    throw new PlanLimitExceededException(
                            "Starter Plan allows a maximum of " + STARTER_MAX_CASHIERS + " cashiers");
                }
            }

            return;
        }

        if (PROFESSIONAL_PLAN.equals(planName) && totalMembers >= PROFESSIONAL_MAX_TEAM_MEMBERS) {
            throw new PlanLimitExceededException(
                    "Professional Plan allows a maximum of " + PROFESSIONAL_MAX_TEAM_MEMBERS + " team members");
        }
    }
}
