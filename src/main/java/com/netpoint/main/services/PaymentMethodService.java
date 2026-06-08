package com.netpoint.main.services;

import com.netpoint.main.dto.requests.AddPaymentMethodRequest;
import com.netpoint.main.dto.requests.UpdatePaymentMethodRequest;
import com.netpoint.main.dto.responses.PaymentMethodResponse;
import com.netpoint.main.exceptions.BadRequestException;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.exceptions.PaymentMethodNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.models.PaymentMethod;
import com.netpoint.main.repositories.CompanyRepository;
import com.netpoint.main.repositories.PaymentMethodRepository;
import com.netpoint.main.utils.CardValidationUtils;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Data
@Service
@Log
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final CompanyRepository       companyRepository;


    //abrunebs payment methods
    @Transactional(readOnly = true)
    public PaymentMethodResponse getPaymentMethod(Integer companyId) {
        Company company = findCompany(companyId);
        return paymentMethodRepository
                .findByCompanyAndStatus(company, "active")
                .map(this::toResponse)
                .orElseThrow(() -> new PaymentMethodNotFoundException("Payment method missing"));
    }



    @Transactional
    public PaymentMethodResponse addPaymentMethod(Integer companyId,
                                                  AddPaymentMethodRequest req) {

        Company company = findCompany(companyId);

        // ert companias marto erti gadaxdis metodi ro hqondes amowmebs
        if (paymentMethodRepository.existsByCompanyAndStatus(company, "active")) {
            throw new BadRequestException(
                    "A payment method already exists. Use PUT to update it.");
        }

        String normalized = CardValidationUtils.normalize(req.cardNumber());
        log.info("The normalized cardNumber looks like: " + normalized);
        CardValidationUtils.validateFormat(normalized);
        CardValidationUtils.validateLuhn(normalized);

        String brand = CardValidationUtils.detectBrand(normalized);
        CardValidationUtils.validateCvc(req.cvc(), brand);
        CardValidationUtils.validateExpiry(req.expMonth(), req.expYear());

        // inaxavs marto usaprtxo ricxvebs da cvc da ragaceebs shlis amis mere
        String last4 = CardValidationUtils.extractLast4(normalized);

        PaymentMethod pm = new PaymentMethod();
        pm.setCompany(company);
        pm.setCardBrand(brand);
        pm.setCardLast4(last4);
        pm.setCardExpMonth(req.expMonth());
        pm.setCardExpYear(req.expYear());
        pm.setCardholderName(req.cardholderName());
        pm.setIsDefault(true);
        pm.setStatus("active");
        pm.setMock_payment_method_id(generateMockToken());

        return toResponse(paymentMethodRepository.save(pm));
    }



    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Integer companyId,
                                                     UpdatePaymentMethodRequest req) {
        Company company = findCompany(companyId);

        PaymentMethod pm = paymentMethodRepository
                .findByCompanyAndStatus(company, "active")
                .orElseThrow(() -> new BadRequestException(
                        "No active payment method found. Use POST to add one."));

        String normalized = CardValidationUtils.normalize(req.cardNumber());
        CardValidationUtils.validateFormat(normalized);
        CardValidationUtils.validateLuhn(normalized);

        String brand = CardValidationUtils.detectBrand(normalized);
        CardValidationUtils.validateCvc(req.cvc(), brand);
        CardValidationUtils.validateExpiry(req.expMonth(), req.expYear());

        String newLast4 = CardValidationUtils.extractLast4(normalized);

        // mock tokens agenerirebs tavidan tu kartis nomeri martla sheicvala
        boolean cardChanged = !newLast4.equals(pm.getCardLast4())
                || !brand.equals(pm.getCardBrand());
        if (cardChanged) {
            pm.setMock_payment_method_id(generateMockToken());
        }

        pm.setCardBrand(brand);
        pm.setCardLast4(newLast4);
        pm.setCardExpMonth(req.expMonth());
        pm.setCardExpYear(req.expYear());
        pm.setCardholderName(req.cardholderName());

        return toResponse(paymentMethodRepository.save(pm));
    }

    // soft deletia es

    @Transactional
    public void deletePaymentMethod(Integer companyId) {
        Company company = findCompany(companyId);

        // washlas blokavs tu pasianze arian
        if (company.getPlan().getCostPerMonth() > 0) {
            throw new BadRequestException(
                    "Cancel your subscription before removing your payment method.");
        }

        PaymentMethod pm = paymentMethodRepository
                .findByCompanyAndStatus(company, "active")
                .orElseThrow(() -> new BadRequestException("No active payment method found."));

        pm.setStatus("removed");
        pm.setIsDefault(false);
        paymentMethodRepository.save(pm);
    }



    private Company findCompany(Integer companyId) {
        return companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));
    }

    private String generateMockToken() {

        return "mock_pm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private PaymentMethodResponse toResponse(PaymentMethod pm) {
        return new PaymentMethodResponse(
                pm.getId(),
                pm.getMock_payment_method_id(),
                pm.getCardBrand(),
                pm.getCardLast4(),
                pm.getCardExpMonth(),
                pm.getCardExpYear(),
                pm.getCardholderName(),
                pm.getIsDefault(),
                pm.getStatus()
        );
    }
}