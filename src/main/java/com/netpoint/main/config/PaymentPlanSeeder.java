package com.netpoint.main.config;

import com.netpoint.main.models.PaymentPlan;
import com.netpoint.main.repositories.PaymentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Log
public class PaymentPlanSeeder implements ApplicationRunner {

    private final PaymentPlanRepository paymentPlanRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing("Starter Plan", 0.0,
                "For small shops and solo entrepreneurs",
                List.of(
                        "Up to 100 products in catalog",
                        "5 Team members (2 Admins + 3 Cashiers)",
                        "Standard Sales Reports",
                        "7-day Activity Log retention"
                ));

        seedIfMissing("Professional Plan", 49.0,
                "For growing businesses with multiple staff",
                List.of(
                        "Unlimited products",
                        "Up to 10 Team members",
                        "30-day Activity Log retention"
                ));

        seedIfMissing("Business Plus Plan", 99.0,
                "For high-volume retailers and large teams",
                List.of(
                        "Unlimited products",
                        "Unlimited Team members",
                        "Full Audit Suite (90-day Activity Log retention)"
                ));
    }

    private void seedIfMissing(String name, Double cost, String purpose, List<String> rules) {
        if (paymentPlanRepository.findByPlanName(name).isEmpty()) {
            PaymentPlan plan = new PaymentPlan();
            plan.setPlanName(name);
            plan.setCostPerMonth(cost);
            plan.setPlanPurpose(purpose);
            plan.setPlanRules(rules);
            paymentPlanRepository.save(plan);
            log.info("Seeded payment plan: " + name);
        } else {
            log.info("Payment plan already exists, skipping: " + name);
        }
    }
}