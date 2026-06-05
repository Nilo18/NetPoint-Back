package com.netpoint.main.services;

import com.netpoint.main.dto.CompanyDTO;
import com.netpoint.main.exceptions.CompanyNotFoundException;
import com.netpoint.main.models.Company;
import com.netpoint.main.repositories.CompanyRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyDTO getCompanyInfo(Integer companyId) {
        Company company = companyRepository.findById(Long.valueOf(companyId))
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        return new CompanyDTO(
                company.getId(),
                company.getEmail(),
                company.getName(),
                company.getIndustry()
        );
    }
}
